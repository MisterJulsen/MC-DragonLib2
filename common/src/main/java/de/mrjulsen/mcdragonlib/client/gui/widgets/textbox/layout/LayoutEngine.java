package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextDocument;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ITextFormatParser;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public final class LayoutEngine {

    public static final int DEFAULT_CACHE_SIZE = 2048;
    public static final int NO_WRAP = -1;

    public static final float DEFAULT_LINE_HEIGHT = 1.0F;
    public static final float MIN_LINE_HEIGHT = 0.1F;
    public static final float MAX_LINE_HEIGHT = 16.0F;

    private static final int MIN_CACHE_SIZE = 64;
    private static final float CACHE_LOAD_FACTOR = 0.75F;
    private static final float LEADING_EPSILON = 0.01F;
    private static final int STATE_CHECKPOINT_INTERVAL = 512;
    private static final String BREAKABLE_CHARACTERS = " \t-/";
    private static final int DEFAULT_INDENT_WIDTH = 12;

    private final TextDocument document;
    private final HeightIndex heightIndex = new HeightIndex();

    private TextMeasurer measurer;
    private ITextFormatParser parser;
    private TextBoxStyle style;
    private ITextTransform textTransform;

    private final Map<Integer, LineLayout> layoutCache;
    private final Map<Integer, Integer> stateCheckpoints = new LinkedHashMap<>();
    private final Map<Object, Object> blockCache = new LinkedHashMap<>();

    private int validStateHighWaterMark;
    private int wrapWidth = NO_WRAP;
    private int viewportWidth;
    private boolean showMarkup = true;
    private float lineHeight = DEFAULT_LINE_HEIGHT;
    private float lineSpacing;
    private int indentWidth = DEFAULT_INDENT_WIDTH;
    private float widestLineSeen;
    private int cachedTextLine = -1;
    private long cachedTextGeneration = -1L;
    private String cachedText;
    private TextStyle baseStyle = TextStyle.DEFAULT;
    private ETextAlignment defaultAlignment = ETextAlignment.LEFT;

    public LayoutEngine(TextDocument document, TextMeasurer measurer, ITextFormatParser parser, TextBoxStyle style) {
        this(document, measurer, parser, style, DEFAULT_CACHE_SIZE);
    }

    public LayoutEngine(TextDocument document, TextMeasurer measurer, ITextFormatParser parser, TextBoxStyle style,
                        int cacheSize) {
        this.document = document;
        this.measurer = measurer;
        this.parser = parser;
        this.style = style;
        this.layoutCache = new LinkedHashMap<>(MIN_CACHE_SIZE, CACHE_LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, LineLayout> eldest) {
                if (size() > Math.max(MIN_CACHE_SIZE, cacheSize)) {
                    heightIndex.forget(eldest.getKey());
                    return true;
                }
                return false;
            }
        };
        heightIndex.reset(document.lineCount(), defaultRowHeight());
    }

    public HeightIndex heights() {
        return heightIndex;
    }

    public TextMeasurer measurer() {
        return measurer;
    }

    public ITextFormatParser parser() {
        return parser;
    }

    public TextBoxStyle style() {
        return style;
    }

    public void setStyle(TextBoxStyle style) {
        this.style = style == null ? TextBoxStyle.VANILLA : style;
        invalidateAll();
    }

    public int indentWidth() {
        return indentWidth;
    }

    public void setIndentWidth(int indentWidth) {
        int next = Math.max(0, indentWidth);
        if (next != this.indentWidth) {
            this.indentWidth = next;
            invalidateAll();
        }
    }

    public int lineCount() {
        return document.lineCount();
    }

    public int wrapWidth() {
        return wrapWidth;
    }

    public int viewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(int viewportWidth) {
        int next = Math.max(0, viewportWidth);
        if (next != this.viewportWidth) {
            this.viewportWidth = next;
            invalidateAll();
        }
    }

    public boolean isWrapping() {
        return wrapWidth > 0;
    }

    public float widestLine() {
        return widestLineSeen;
    }

    public void setMeasurer(TextMeasurer measurer) {
        this.measurer = measurer;
        heightIndex.setDefaultHeight(defaultRowHeight());
        invalidateAll();
    }

    public void setParser(ITextFormatParser parser) {
        this.parser = parser;
        invalidateAll();
    }

    public TextStyle baseStyle() {
        return baseStyle;
    }

    public void setBaseStyle(TextStyle baseStyle) {
        TextStyle next = baseStyle == null ? TextStyle.DEFAULT : baseStyle;
        if (!next.equals(this.baseStyle)) {
            this.baseStyle = next;
            invalidateAll();
        }
    }

    public float lineHeight() {
        return lineHeight;
    }

    public void setLineHeight(float lineHeight) {
        float next = Math.max(MIN_LINE_HEIGHT, Math.min(lineHeight, MAX_LINE_HEIGHT));
        if (next != this.lineHeight) {
            this.lineHeight = next;
            invalidateAll();
        }
    }

    public float lineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        if (lineSpacing != this.lineSpacing) {
            this.lineSpacing = lineSpacing;
            invalidateAll();
        }
    }

    public float defaultRowHeight() {
        return rowHeightOf(measurer.lineHeight(baseStyle));
    }

    public ITextTransform textTransform() {
        return textTransform;
    }

    public void setTextTransform(ITextTransform textTransform) {
        if (textTransform != this.textTransform) {
            this.textTransform = textTransform;
            invalidateAll();
        }
    }

    public String textOf(int line) {
        if (line == cachedTextLine && cachedTextGeneration == document.generation()) {
            return cachedText;
        }
        String text = document.getLine(line);
        String result = textTransform == null ? text : textTransform.apply(line, text);
        cachedTextLine = line;
        cachedTextGeneration = document.generation();
        cachedText = result;
        return result;
    }

    public ParsedLine parsedLineAt(int line) {
        if (line < 0 || line >= document.lineCount()) {
            return null;
        }
        return parser.parseLine(textOf(line), stateAt(line));
    }

    public TextStyle styleAt(int offset) {
        int clamped = document.clampOffset(offset);
        int line = document.lineOfOffset(clamped);
        int column = clamped - document.lineStart(line);

        StyledSpan match = null;
        for (StyledSpan span : layoutOf(line).parsed().spans()) {
            if (span.isMarkup() || span.isEmpty()) {
                continue;
            }
            if (column >= span.start() && column < span.end()) {
                match = span;
                break;
            }
            if (column == span.end()) {
                match = span;
            }
        }
        return match == null ? baseStyle : baseStyle.merge(match.style());
    }

    public String plainText(int fromOffset, int toOffset) {
        int from = document.clampOffset(Math.min(fromOffset, toOffset));
        int to = document.clampOffset(Math.max(fromOffset, toOffset));
        if (from >= to) {
            return "";
        }

        StringBuilder builder = new StringBuilder(to - from);
        int firstLine = document.lineOfOffset(from);
        int lastLine = document.lineOfOffset(to);

        for (int line = firstLine; line <= lastLine; line++) {
            String raw = document.getLine(line);
            int lineStart = document.lineStart(line);
            if (line > firstLine) {
                builder.append('\n');
            }
            for (StyledSpan span : layoutOf(line).parsed().spans()) {
                if (span.isMarkup()) {
                    continue;
                }
                int start = Math.max(span.start(), from - lineStart);
                int end = Math.min(Math.min(span.end(), raw.length()), to - lineStart);
                if (start < end) {
                    builder.append(raw, start, end);
                }
            }
        }
        return builder.toString();
    }

    public void setWrapWidth(int wrapWidth) {
        int next = wrapWidth > 0 ? wrapWidth : NO_WRAP;
        if (next != this.wrapWidth) {
            this.wrapWidth = next;
            invalidateAll();
        }
    }

    public void setShowMarkup(boolean showMarkup) {
        if (showMarkup != this.showMarkup) {
            this.showMarkup = showMarkup;
            invalidateAll();
        }
    }

    public boolean showsMarkup() {
        return showMarkup;
    }

    public ETextAlignment defaultAlignment() {
        return defaultAlignment;
    }

    public void setDefaultAlignment(ETextAlignment alignment) {
        ETextAlignment next = alignment == null ? ETextAlignment.LEFT : alignment;
        if (next != this.defaultAlignment) {
            this.defaultAlignment = next;
            invalidateAll();
        }
    }

    public Map<Object, Object> blockCache() {
        return blockCache;
    }

    public void invalidateAll() {
        cachedTextLine = -1;
        cachedText = null;
        layoutCache.clear();
        stateCheckpoints.clear();
        blockCache.clear();
        validStateHighWaterMark = 0;
        widestLineSeen = 0.0F;
        heightIndex.reset(document.lineCount(), defaultRowHeight());
    }

    public void onLinesChanged(int firstLine, int removedLines, int insertedLines) {
        int from = Math.max(0, firstLine - 1);

        if (removedLines == 0 && insertedLines == 0) {
            layoutCache.remove(firstLine);
            layoutCache.remove(from);
            heightIndex.setLineCount(document.lineCount());
        } else {
            layoutCache.keySet().removeIf(line -> line >= from);
            heightIndex.splice(firstLine, removedLines, insertedLines);
            heightIndex.setLineCount(document.lineCount());
        }

        validStateHighWaterMark = Math.min(validStateHighWaterMark, firstLine);
        stateCheckpoints.keySet().removeIf(line -> line > firstLine);

        blockCache.clear();
        layoutCache.entrySet().removeIf(entry -> entry.getValue().attachment() != null);

        int lastLine = document.lineCount() - 1;
        layoutOf(Math.min(from, lastLine));
        if (firstLine != from) {
            layoutOf(Math.min(firstLine, lastLine));
        }
    }

    public LineLayout layoutOf(int line) {
        int clamped = Math.max(0, Math.min(line, document.lineCount() - 1));
        LineLayout cached = layoutCache.get(clamped);
        if (cached != null && cached.isValid(document.generation(), wrapWidth)) {
            return cached;
        }

        LineLayout layout = buildLayout(clamped);
        layoutCache.put(clamped, layout);
        heightIndex.setHeight(clamped, layout.height());
        widestLineSeen = Math.max(widestLineSeen, layout.width());
        return layout;
    }

    private LineLayout buildLayout(int line) {
        String text = textOf(line);
        ParsedLine parsed = parser.parseLine(text, stateAt(line));
        List<StyledSpan> spans = withBaseStyle(showMarkup ? parsed.spans() : withoutMarkup(parsed.spans()));

        BlockLayoutContext context = new BlockLayoutContext(this, line, text, parsed, spans, indentFor(parsed));
        IBlockLayout blockLayout = parser.blockLayout(parsed.kind());
        BlockLayoutResult result = blockLayout == null ? null : blockLayout.layout(context);

        List<VisualRow> rows = result == null || result.isEmpty() ? context.textRows() : result.rows();
        rows = align(rows, parsed);

        float height = 0.0F;
        float width = 0.0F;
        for (VisualRow row : rows) {
            height += row.height();
            width = Math.max(width, row.indentX() + row.width());
        }
        return new LineLayout(line, document.generation(), wrapWidth, parsed, rows, height, width,
                result == null ? null : result.attachment());
    }

    private List<VisualRow> align(List<VisualRow> rows, ParsedLine parsed) {
        if (!parsed.kind().allowsAlignment()) {
            return rows;
        }
        ETextAlignment alignment = parsed.align() == null ? defaultAlignment : parsed.align();
        if (alignment == ETextAlignment.LEFT) {
            return rows;
        }
        float available = isWrapping() ? wrapWidth : viewportWidth;
        if (available <= 0.0F) {
            return rows;
        }

        List<VisualRow> result = new ArrayList<>(rows.size());
        for (VisualRow row : rows) {
            float slack = available - row.indentX() - row.width();
            if (slack <= 0.0F) {
                result.add(row);
                continue;
            }
            float offset = alignment == ETextAlignment.CENTER ? slack / 2.0F : slack;
            result.add(row.withIndentX(row.indentX() + offset));
        }
        return result;
    }

    List<VisualRow> textRows(String text, List<StyledSpan> spans, ParsedLine parsed, float indent, float rightInset) {
        if (!isWrapping()) {
            return List.of(singleRow(text, spans, indent, 0, text.length()));
        }
        float continuationIndent = parsed.kind().hasHangingIndent() ? indent + indentWidth : indent;
        return wrapIntoRows(text, spans, indent, continuationIndent, rightInset);
    }

    VisualRow singleRow(String text, List<StyledSpan> spans, float indent, int from, int to) {
        List<StyledSpan> clipped = clipSpans(spans, from, to);
        float width = 0.0F;
        float height = measurer.lineHeight(baseStyle);
        for (StyledSpan span : clipped) {
            width += measurer.width(text, span.start(), span.end(), span.style());
            height = Math.max(height, measurer.lineHeight(span.style()));
        }
        return withLeading(VisualRow.flowing(from, to, indent, width, height, clipped));
    }

    private List<VisualRow> wrapIntoRows(String text, List<StyledSpan> spans, float indent, float continuationIndent,
                                         float rightInset) {
        List<VisualRow> rows = new ArrayList<>(2);
        int length = text.length();
        if (length == 0) {
            rows.add(singleRow(text, spans, indent, 0, 0));
            return rows;
        }

        int cursor = 0;
        while (cursor < length) {
            float currentIndent = rows.isEmpty() ? indent : continuationIndent;
            float available = wrapWidth - currentIndent - rightInset;
            int end = findBreak(text, spans, cursor, length, available);
            rows.add(singleRow(text, spans, currentIndent, cursor, end));
            cursor = end;
        }
        return rows;
    }

    private float rowHeightOf(float contentHeight) {
        return Math.max(1.0F, contentHeight * lineHeight + lineSpacing);
    }

    VisualRow withLeading(VisualRow row) {
        float leading = rowHeightOf(row.height()) - row.height();
        if (Math.abs(leading) < LEADING_EPSILON) {
            return row;
        }
        float above = leading / 2.0F;
        return padded(row, above, leading - above);
    }

    static VisualRow padded(VisualRow row, float above, float below) {
        if (above == 0.0F && below == 0.0F) {
            return row;
        }
        return new VisualRow(row.startColumn(), row.endColumn(), row.indentX(), row.width(),
                row.height() + above + below, row.baseline() + above, row.spans(), row.spanOffsets());
    }

    private int findBreak(String text, List<StyledSpan> spans, int from, int limit, float available) {
        float used = 0.0F;
        int lastBreakable = -1;

        for (StyledSpan span : spans) {
            if (span.end() <= from) {
                continue;
            }
            int start = Math.max(span.start(), from);
            if (start >= limit) {
                break;
            }
            int end = Math.min(span.end(), limit);

            float spanWidth = measurer.width(text, start, end, span.style());
            if (used + spanWidth <= available) {
                for (int i = start; i < end; i++) {
                    if (isBreakable(text.charAt(i))) {
                        lastBreakable = i + 1;
                    }
                }
                used += spanWidth;
                continue;
            }

            String runText = text.substring(start, end);
            int fits = measurer.fitCharacters(runText, span.style(), available - used);
            int hardEnd = start + Math.max(fits, 0);

            for (int i = start; i < hardEnd; i++) {
                if (isBreakable(text.charAt(i))) {
                    lastBreakable = i + 1;
                }
            }
            if (lastBreakable > from) {
                return lastBreakable;
            }
            return Math.max(hardEnd, from + 1);
        }
        return limit;
    }

    private static boolean isBreakable(char c) {
        return BREAKABLE_CHARACTERS.indexOf(c) >= 0;
    }

    private float indentFor(ParsedLine parsed) {
        float indent = parsed.indent() * (float) indentWidth;
        if (showMarkup) {
            return indent;
        }
        return parsed.kind().reservesMarkerSpace() ? indent + indentWidth : indent;
    }

    private List<StyledSpan> withBaseStyle(List<StyledSpan> spans) {
        if (baseStyle.equals(TextStyle.DEFAULT) || spans.isEmpty()) {
            return spans;
        }
        List<StyledSpan> result = new ArrayList<>(spans.size());
        for (StyledSpan span : spans) {
            result.add(new StyledSpan(span.start(), span.end(), baseStyle.merge(span.style())));
        }
        return result;
    }

    private static List<StyledSpan> withoutMarkup(List<StyledSpan> spans) {
        List<StyledSpan> result = new ArrayList<>(spans.size());
        for (StyledSpan span : spans) {
            if (!span.isMarkup()) {
                result.add(span);
            }
        }
        return result;
    }

    static List<StyledSpan> clipSpans(List<StyledSpan> spans, int from, int to) {
        List<StyledSpan> result = new ArrayList<>(Math.min(spans.size(), 8));
        for (StyledSpan span : spans) {
            if (span.end() <= from) {
                continue;
            }
            if (span.start() >= to) {
                break;
            }
            StyledSpan clipped = span.sub(from, to);
            if (clipped != null) {
                result.add(clipped);
            }
        }
        return result;
    }

    private int stateAt(int line) {
        if (line == 0) {
            return ITextFormatParser.INITIAL_STATE;
        }

        int checkpointLine = 0;
        int state = ITextFormatParser.INITIAL_STATE;
        int nearest = (line / STATE_CHECKPOINT_INTERVAL) * STATE_CHECKPOINT_INTERVAL;

        for (int candidate = nearest; candidate > 0; candidate -= STATE_CHECKPOINT_INTERVAL) {
            Integer cached = candidate <= validStateHighWaterMark ? stateCheckpoints.get(candidate) : null;
            if (cached != null) {
                checkpointLine = candidate;
                state = cached;
                break;
            }
        }

        for (int i = checkpointLine; i < line; i++) {
            state = parser.nextState(textOf(i), state);
            int next = i + 1;
            if (next % STATE_CHECKPOINT_INTERVAL == 0) {
                stateCheckpoints.put(next, state);
                validStateHighWaterMark = Math.max(validStateHighWaterMark, next);
            }
        }
        return state;
    }
}
