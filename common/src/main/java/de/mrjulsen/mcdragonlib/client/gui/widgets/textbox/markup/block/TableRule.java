package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.BlockLayoutContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.BlockLayoutResult;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.IBlockLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.VisualRow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.BlockDecoration;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.IBlockDecorator;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public final class TableRule implements IBlockRule, IBlockLayout, IBlockDecorator {

    public static final BlockKind KIND_ROW = BlockKind.register("table_row", BlockKind.TRAIT_NO_ALIGNMENT);
    public static final BlockKind KIND_DELIMITER = BlockKind.register("table_delimiter",
            BlockKind.TRAIT_DECORATION | BlockKind.TRAIT_NO_ALIGNMENT);

    public static final TableRule DEFAULT = new TableRule('|', '-', ':');

    private static final int MAX_TABLE_LINES = 512;

    public int headerBackground = 0x30FFFFFF;
    public int headerText = 0xFFFFFFFF;
    public int headerRule = 0xFF909090;
    public int rowStripe = 0x0CFFFFFF;
    public int gridColor = 0xFF606060;
    public int gridThickness = 1;
    public int cellPadding = 4;
    public int cellPaddingV = 1;
    public int fontDescent = 2;
    public int delimiterHeight = 4;

    private final char separator;
    private final char fill;
    private final char alignmentMark;

    private record TableInfo(int first, int last, float[] columns, ETextAlignment[] alignments) {
    }

    public TableRule(char separator, char fill, char alignmentMark) {
        this.separator = separator;
        this.fill = fill;
        this.alignmentMark = alignmentMark;
    }

    @Override
    public String reservedCharacters() {
        return String.valueOf(separator);
    }

    @Override
    public ParsedLine parse(BlockContext context) {
        int start = context.contentStart();
        int length = context.length();
        if (context.isBlank() || context.charAt(start) != separator || length - 1 <= start || context.charAt(length - 1) != separator) {
            return null;
        }

        List<Integer> bounds = new ArrayList<>(8);
        boolean delimiterRow = true;
        int cellStart = start + 1;

        bounds.add(start);
        context.emitMarkup(0, cellStart);

        for (int i = cellStart; i < length; i++) {
            if (context.charAt(i) != separator || MarkupText.isEscaped(context.line(), i)) {
                continue;
            }
            if (!isDelimiterCell(context.line(), cellStart, i)) {
                delimiterRow = false;
            }
            context.parseInline(cellStart, i, TextStyle.DEFAULT);
            context.emitMarkup(i, i + 1);
            bounds.add(i);
            cellStart = i + 1;
        }

        int[] cellBounds = new int[bounds.size()];
        for (int i = 0; i < cellBounds.length; i++) {
            cellBounds[i] = bounds.get(i);
        }

        if (!delimiterRow) {
            return context.build(KIND_ROW, 0, false, null, cellBounds);
        }
        return context.build(KIND_DELIMITER, 0, false, alignmentCodes(context.line(), cellBounds, length), cellBounds);
    }

    private String alignmentCodes(CharSequence line, int[] bounds, int length) {
        StringBuilder codes = new StringBuilder(bounds.length);
        for (int i = 0; i < bounds.length; i++) {
            int from = Math.min(bounds[i] + 1, length);
            int to = i + 1 < bounds.length ? Math.min(bounds[i + 1], length) : length;
            codes.append(TableAlignment.code(from < to ? columnAlignment(line, from, to) : ETextAlignment.LEFT));
        }
        return codes.toString();
    }

    public ETextAlignment columnAlignment(CharSequence line, int start, int end) {
        int from = start;
        int to = end;
        while (from < to && line.charAt(from) == ' ') {
            from++;
        }
        while (to > from && line.charAt(to - 1) == ' ') {
            to--;
        }
        if (to - from < 2) {
            return ETextAlignment.LEFT;
        }
        boolean left = line.charAt(from) == alignmentMark;
        boolean right = line.charAt(to - 1) == alignmentMark;
        if (left && right) {
            return ETextAlignment.CENTER;
        }
        return right ? ETextAlignment.RIGHT : ETextAlignment.LEFT;
    }

    private boolean isDelimiterCell(CharSequence line, int start, int end) {
        boolean sawFill = false;
        for (int i = start; i < end; i++) {
            char c = line.charAt(i);
            if (c == fill) {
                sawFill = true;
            } else if (c != alignmentMark && c != ' ') {
                return false;
            }
        }
        return sawFill;
    }

    @Override
    public IBlockLayout layoutOf(BlockKind kind) {
        return isTable(kind) ? this : null;
    }

    @Override
    public BlockLayoutResult layout(BlockLayoutContext context) {
        int[] bounds = context.parsed().dataAs(int[].class);
        TableGeometry table = context.showsMarkup() ? null : geometryAt(context);
        if (table == null || bounds == null) {
            return BlockLayoutResult.of(context.textRows());
        }

        String text = context.text();
        List<StyledSpan> clipped = headerStyled(context.clipSpans(0, text.length()), table);

        float[] runWidths = new float[clipped.size()];
        int[] cells = new int[clipped.size()];
        float[] cellWidths = new float[table.columnCount() + 1];
        float height = context.measurer().lineHeight(context.baseStyle());

        for (int i = 0; i < clipped.size(); i++) {
            StyledSpan span = clipped.get(i);
            runWidths[i] = context.width(span.start(), span.end(), span.style());
            cells[i] = Math.min(cellIndexOf(bounds, span.start()), cellWidths.length - 1);
            cellWidths[cells[i]] += runWidths[i];
            height = Math.max(height, context.measurer().lineHeight(span.style()));
        }

        float[] offsets = new float[clipped.size()];
        int currentCell = -1;
        float cursorX = 0.0F;
        float width = 0.0F;

        for (int i = 0; i < clipped.size(); i++) {
            if (cells[i] != currentCell) {
                currentCell = cells[i];
                cursorX = cellOrigin(table, currentCell, cellWidths[currentCell]);
            }
            offsets[i] = cursorX;
            cursorX += runWidths[i];
            width = Math.max(width, cursorX);
        }

        float rowWidth = Math.max(width, table.totalWidth());
        if (table.delimiter()) {
            float gap = Math.max(1.0F, delimiterHeight);
            return BlockLayoutResult.of(new VisualRow(0, text.length(), context.indent(), rowWidth, gap, gap,
                    clipped, offsets), table);
        }

        float rowHeight = (float) Math.ceil(height);
        VisualRow row = context.withLeading(new VisualRow(0, text.length(), context.indent(), rowWidth,
                rowHeight, rowHeight, clipped, offsets));
        return BlockLayoutResult.of(context.padded(row, cellPaddingV + fontDescent, cellPaddingV), table);
    }

    private List<StyledSpan> headerStyled(List<StyledSpan> spans, TableGeometry table) {
        if (!table.header()) {
            return spans;
        }
        List<StyledSpan> result = new ArrayList<>(spans.size());
        for (StyledSpan span : spans) {
            TextStyle style = span.style();
            boolean plain = !span.isMarkup() && !style.hasLink() && style.color() == TextStyle.DEFAULT.color();
            result.add(plain ? new StyledSpan(span.start(), span.end(), style.withColor(headerText)) : span);
        }
        return result;
    }

    private float cellOrigin(TableGeometry table, int cell, float cellWidth) {
        float start = table.columnStart(cell);
        float padding = cellPadding;
        return switch (table.alignment(cell)) {
            case CENTER -> start + Math.max(padding, (table.columnWidth(cell) - cellWidth) / 2.0F);
            case RIGHT -> start + Math.max(padding, table.columnWidth(cell) - padding - cellWidth);
            default -> start + padding;
        };
    }

    private TableGeometry geometryAt(BlockLayoutContext context) {
        TableInfo info = tableAt(context);
        if (info == null) {
            return null;
        }
        int line = context.line();
        boolean delimiter = context.parsed().kind() == KIND_DELIMITER;
        return new TableGeometry(info.columns(), info.alignments(), line - info.first(),
                info.last() - info.first() + 1, line == info.first() && !delimiter, delimiter);
    }

    private TableInfo tableAt(BlockLayoutContext context) {
        int line = context.line();
        int first = line;
        while (first > 0 && line - first < MAX_TABLE_LINES && isTableLine(context, first - 1)) {
            first--;
        }

        Map<Integer, TableInfo> cache = cache(context);
        TableInfo cached = cache.get(first);
        if (cached != null) {
            return cached;
        }

        int last = line;
        int lineCount = context.lineCount();
        while (last + 1 < lineCount && last - first < MAX_TABLE_LINES && isTableLine(context, last + 1)) {
            last++;
        }
        if (last - first >= MAX_TABLE_LINES) {
            return null;
        }

        List<Float> widths = new ArrayList<>(8);
        ETextAlignment[] alignments = null;
        for (int row = first; row <= last; row++) {
            ParsedLine parsed = context.parsedAt(row);
            measureRow(context, context.textAt(row), parsed, widths);
            if (alignments == null && parsed.kind() == KIND_DELIMITER) {
                alignments = TableAlignment.parse(parsed.meta());
            }
        }

        float[] columns = new float[widths.size()];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = widths.get(i) + cellPadding * 2.0F;
        }

        TableInfo info = new TableInfo(first, last, columns, alignments);
        cache.put(first, info);
        return info;
    }

    private void measureRow(BlockLayoutContext context, String text, ParsedLine parsed, List<Float> widths) {
        int[] bounds = parsed.dataAs(int[].class);
        if (bounds == null) {
            return;
        }

        float cellWidth = 0.0F;
        int currentCell = -1;
        for (StyledSpan span : parsed.spans()) {
            if (span.isMarkup() || parsed.kind() == KIND_DELIMITER) {
                continue;
            }
            int cell = cellIndexOf(bounds, span.start());
            if (cell != currentCell) {
                if (currentCell >= 0) {
                    recordWidth(widths, currentCell, cellWidth);
                }
                currentCell = cell;
                cellWidth = 0.0F;
            }
            cellWidth += context.measurer().width(text, span.start(), span.end(),
                    context.baseStyle().merge(span.style()));
        }
        if (currentCell >= 0) {
            recordWidth(widths, currentCell, cellWidth);
        }
    }

    private static void recordWidth(List<Float> widths, int cell, float width) {
        while (widths.size() <= cell) {
            widths.add(0.0F);
        }
        widths.set(cell, Math.max(widths.get(cell), width));
    }

    private boolean isTableLine(BlockLayoutContext context, int line) {
        ParsedLine parsed = context.parsedAt(line);
        return parsed != null && isTable(parsed.kind());
    }

    private Map<Integer, TableInfo> cache(BlockLayoutContext context) {
        Object cache = context.cache().computeIfAbsent(this, key -> new LinkedHashMap<Integer, TableInfo>());
        return (Map<Integer, TableInfo>) cache;
    }

    private static int cellIndexOf(int[] bounds, int column) {
        int cell = 0;
        while (cell < bounds.length && column >= bounds[cell]) {
            cell++;
        }
        return Math.max(0, cell - 1);
    }

    private static boolean isTable(BlockKind kind) {
        return kind == KIND_ROW || kind == KIND_DELIMITER;
    }

    @Override
    public IBlockDecorator decoratorOf(BlockKind kind) {
        return isTable(kind) ? this : null;
    }

    @Override
    public void render(BlockDecoration d) {
        TableGeometry table = d.layout().attachmentAs(TableGeometry.class);
        if (table == null) {
            return;
        }
        int x = d.contentX();
        int top = d.top();
        int bottom = d.bottom();
        int right = x + Math.round(table.totalWidth());
        if (right <= x) {
            return;
        }

        if (table.header() || table.delimiter()) {
            d.graphics().graphics().fill(x, top, right, bottom, headerBackground);
        } else if (table.striped()) {
            d.graphics().graphics().fill(x, top, right, bottom, rowStripe);
        }

        for (int column = 0; column <= table.columnCount(); column++) {
            int columnX = column == table.columnCount()
                    ? right - gridThickness
                    : x + Math.round(table.columnStart(column));
            d.graphics().graphics().fill(columnX, top, columnX + gridThickness, bottom, gridColor);
        }

        if (table.rowIndex() == 0) {
            d.graphics().graphics().fill(x, top, right, top + gridThickness, gridColor);
        }
        if (table.last()) {
            d.graphics().graphics().fill(x, bottom - gridThickness, right, bottom, gridColor);
        }
        if (table.delimiter()) {
            d.graphics().graphics().fill(x, bottom - gridThickness, right, bottom, headerRule);
        }
    }
}
