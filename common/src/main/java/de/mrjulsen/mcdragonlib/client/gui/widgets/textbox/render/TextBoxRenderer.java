package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextDocument;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextRange;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.LayoutEngine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.LineLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.TextGeometry;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.TextMeasurer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.VisualRow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

public class TextBoxRenderer {

    protected static final int ALPHA_SHIFT = 24;
    protected static final int INVERTED_COLOR = 0xFF0000FF;
    protected static final int CLIP_LOOKAHEAD = 2;

    protected record VisibleRun(String text, float x) {}

    protected record VisibleColumns(int from, int to) {

        public boolean contains(int fromColumn, int toColumn) {
            return toColumn >= from && fromColumn <= to;
        }
    }

    private final TextDocument document;
    private final LayoutEngine layout;
    private final Map<BlockKind, IBlockDecorator> decorators = new HashMap<>();

    public TextBoxRenderer(TextDocument document, LayoutEngine layout) {
        this.document = document;
        this.layout = layout;
    }

    public void decorate(BlockKind kind, IBlockDecorator decorator) {
        if (decorator == null) {
            decorators.remove(kind);
        } else {
            decorators.put(kind, decorator);
        }
    }

    public IBlockDecorator decoratorOf(BlockKind kind) {
        IBlockDecorator decorator = decorators.get(kind);
        return decorator != null ? decorator : layout.parser().blockDecorator(kind);
    }

    public LayoutEngine layout() {
        return layout;
    }

    public void render(DLGuiGraphics graphics, TextBoxRenderContext ctx) {
        TextBoxStyle boxStyle = layout.style();
        TextMeasurer measurer = layout.measurer();

        int firstLine = layout.heights().lineAtOffset((float) ctx.scrollY());
        float firstTop = layout.heights().offsetOf(firstLine) - (float) ctx.scrollY();
        int caretLine = document.lineOfOffset(ctx.caretOffset());

        if (ctx.hasGutter()) {
            Rectangle gutter = ctx.gutterArea();
            graphics.graphics().fill((int) gutter.x(), (int) gutter.y(),
                    (int) (gutter.x() + gutter.width()), (int) (gutter.y() + gutter.height()),
                    boxStyle.gutterBackground);
        }

        if (ctx.hasTextClip()) {
            GuiUtils.enableScissor(graphics, ctx.textClip());
        }
        float lineTop = firstTop;
        int rows = 0;
        for (int line = firstLine; line < document.lineCount() && lineTop < ctx.textHeight(); line++) {
            if (ctx.hasRowLimit() && rows >= ctx.maxRows()) {
                break;
            }
            LineLayout lineLayout = layout.layoutOf(line);

            if (lineTop + lineLayout.height() >= 0.0F) {
                rows = renderLine(graphics, ctx, lineLayout, line, lineTop, caretLine, boxStyle, measurer, rows);
            } else {
                rows += lineLayout.rowCount();
            }
            lineTop += lineLayout.height();
        }
        if (ctx.hasTextClip()) {
            if (ctx.clip() != null) {
                GuiUtils.enableScissor(graphics, ctx.clip());
            } else {
                GuiUtils.disableScissor(graphics);
            }
        }

        if (!ctx.hasGutter()) {
            return;
        }
        lineTop = firstTop;
        for (int line = firstLine; line < document.lineCount() && lineTop < ctx.textHeight(); line++) {
            LineLayout lineLayout = layout.layoutOf(line);

            if (lineTop + lineLayout.height() >= 0.0F) {
                renderGutter(graphics, ctx, line, lineTop, line == caretLine, boxStyle, measurer);
            }
            lineTop += lineLayout.height();
        }
    }

    protected int renderLine(DLGuiGraphics graphics, TextBoxRenderContext ctx, LineLayout lineLayout, int line,
                             float lineTop, int caretLine, TextBoxStyle boxStyle, TextMeasurer measurer, int rowsBefore) {
        String lineText = layout.textOf(line);
        int lineStart = document.lineStart(line);
        ParsedLine parsed = lineLayout.parsed();

        boolean isCaretLine = line == caretLine;
        boolean highlighted = ctx.lineHighlight() != null && ctx.lineHighlight().test(line);

        int rows = rowsBefore;
        float rowTop = lineTop;
        for (int rowIndex = 0; rowIndex < lineLayout.rowCount(); rowIndex++) {
            if (ctx.hasRowLimit() && rows >= ctx.maxRows()) {
                break;
            }
            boolean lastRow = ctx.hasRowLimit() && rows == ctx.maxRows() - 1;
            String ellipsis = lastRow && ctx.hasEllipsis() ? ctx.ellipsis() : null;

            VisualRow row = lineLayout.row(rowIndex);
            float screenY = ctx.textTop() + rowTop;

            int top = Math.round(screenY);
            int bottom = top + Math.round(row.height());

            renderRowBackground(graphics, ctx, row, top, bottom, isCaretLine, highlighted, boxStyle);
            renderBlockDecoration(graphics, ctx, lineLayout, row, rowIndex, top, bottom, boxStyle, measurer);

            if (!parsed.kind().isDecoration() || layout.showsMarkup()) {
                int textBottom = top + Math.round(row.baseline());
                renderRowText(graphics, ctx, row, lineText, textBottom, boxStyle, measurer, ellipsis);
            }

            if (ctx.hasHighlights() || !ctx.selection().isEmpty()) {
                VisibleColumns visible = visibleColumns(ctx, row, lineText, measurer);
                renderHighlights(graphics, ctx, row, lineText, lineStart, top, bottom, boxStyle, measurer, visible);
                renderSelection(graphics, ctx, row, lineText, lineStart, top, bottom, boxStyle, measurer, visible);
            }
            renderCaret(graphics, ctx, row, lineText, lineStart, top, bottom, boxStyle, measurer);

            rowTop += row.height();
            rows++;
        }
        return rows;
    }

    protected void renderRowBackground(DLGuiGraphics graphics, TextBoxRenderContext ctx, VisualRow row,
                                       int top, int bottom, boolean isCaretLine, boolean highlighted,
                                       TextBoxStyle boxStyle) {
        int color = highlighted ? boxStyle.lineHighlight
                : (isCaretLine && ctx.focused() && ctx.highlightCurrentLine() ? boxStyle.currentLineHighlight : 0);
        if ((color >>> ALPHA_SHIFT) == 0) {
            return;
        }
        graphics.graphics().fill(ctx.textLeft(), top, ctx.textLeft() + ctx.textWidth(), bottom, color);
    }

    protected void renderBlockDecoration(DLGuiGraphics graphics, TextBoxRenderContext ctx, LineLayout lineLayout,
                                         VisualRow row, int rowIndex, int top, int bottom, TextBoxStyle boxStyle,
                                         TextMeasurer measurer) {
        if (layout.showsMarkup()) {
            return;
        }
        IBlockDecorator decorator = decoratorOf(lineLayout.parsed().kind());
        if (decorator == null) {
            return;
        }

        int contentX = (int) Math.round(ctx.textLeft() - ctx.scrollX() + row.indentX());
        int lineX = (int) Math.round(ctx.textLeft() - ctx.scrollX() + lineLayout.row(0).indentX());
        decorator.render(new BlockDecoration(graphics, ctx, lineLayout, row, rowIndex, top, bottom,
                contentX, lineX, lineX - layout.indentWidth(), boxStyle, measurer));
    }

    protected VisibleColumns visibleColumns(TextBoxRenderContext ctx, VisualRow row, String lineText,
                                           TextMeasurer measurer) {
        float left = (float) ctx.scrollX();
        float right = left + ctx.textWidth();
        if (left <= row.indentX() && row.indentX() + row.width() <= right) {
            return new VisibleColumns(row.startColumn(), row.endColumn());
        }

        int from = TextGeometry.columnAtX(measurer, row, lineText, left);
        int to = TextGeometry.columnAtX(measurer, row, lineText, right);
        return new VisibleColumns(from, Math.min(to + 1, row.endColumn()));
    }

    protected void renderHighlights(DLGuiGraphics graphics, TextBoxRenderContext ctx, VisualRow row, String lineText,
                                    int lineStart, int top, int bottom, TextBoxStyle boxStyle, TextMeasurer measurer,
                                    VisibleColumns visible) {
        if (!ctx.hasHighlights()) {
            return;
        }
        int rowStart = lineStart + row.startColumn();
        int rowEnd = lineStart + row.endColumn();
        ctx.highlights().forEachHighlight(rowStart, rowEnd + 1, (start, end, active) ->
                fillRange(graphics, ctx, row, lineText, lineStart, top, bottom, start, end,
                        active ? boxStyle.searchHighlightActive : boxStyle.searchHighlight, measurer, false, visible));
    }

    protected void renderSelection(DLGuiGraphics graphics, TextBoxRenderContext ctx, VisualRow row, String lineText,
                                   int lineStart, int top, int bottom, TextBoxStyle boxStyle, TextMeasurer measurer,
                                   VisibleColumns visible) {
        TextRange selection = ctx.selection();
        if (selection.isEmpty()) {
            return;
        }
        fillRange(graphics, ctx, row, lineText, lineStart, top, bottom, selection.start(), selection.end(),
                ctx.focused() ? boxStyle.selection : boxStyle.selectionInactive, measurer, true, visible);
    }

    protected void fillRange(DLGuiGraphics graphics, TextBoxRenderContext ctx, VisualRow row, String lineText,
                             int lineStart, int top, int bottom, int startOffset, int endOffset, int color,
                             TextMeasurer measurer, boolean includeLineBreak, VisibleColumns visible) {
        int rowStart = lineStart + row.startColumn();
        int rowEnd = lineStart + row.endColumn();
        if (endOffset < rowStart || startOffset > rowEnd) {
            return;
        }

        int fromColumn = Math.max(startOffset - lineStart, row.startColumn());
        int toColumn = Math.min(endOffset - lineStart, row.endColumn());
        if (!visible.contains(fromColumn, toColumn)) {
            return;
        }

        int clippedTo = Math.min(toColumn, visible.to());
        float x1 = TextGeometry.xOfColumn(measurer, row, lineText, Math.max(fromColumn, visible.from()));
        float x2 = TextGeometry.xOfColumn(measurer, row, lineText, clippedTo);
        if (includeLineBreak && endOffset > rowEnd && clippedTo == toColumn) {
            x2 += measurer.baseLineHeight() * layout.style().lineBreakSelectionRatio;
        }

        int left = (int) Math.round(ctx.textLeft() - ctx.scrollX() + x1);
        int right = (int) Math.round(ctx.textLeft() - ctx.scrollX() + x2);
        if (right <= left) {
            return;
        }
        graphics.graphics().fill(left, top, right, bottom, color);
    }

    protected void renderRowText(DLGuiGraphics graphics, TextBoxRenderContext ctx, VisualRow row, String lineText,
                                 int rowBottom, TextBoxStyle boxStyle, TextMeasurer measurer) {
        renderRowText(graphics, ctx, row, lineText, rowBottom, boxStyle, measurer, null);
    }

    protected void renderRowText(DLGuiGraphics graphics, TextBoxRenderContext ctx, VisualRow row, String lineText,
                                 int rowBottom, TextBoxStyle boxStyle, TextMeasurer measurer, String ellipsis) {
        float base = ctx.textLeft() - (float) ctx.scrollX() + row.indentX();
        float right = ctx.textLeft() + ctx.textWidth();
        float limit = ellipsis == null ? right : right - measurer.width(ellipsis, TextStyle.DEFAULT);
        float flow = 0.0F;
        float cursorX = base;
        TextStyle lastStyle = TextStyle.DEFAULT;
        boolean truncated = false;

        List<StyledSpan> spans = row.spans();
        for (int i = 0; i < spans.size(); i++) {
            StyledSpan span = spans.get(i);
            int end = Math.min(span.end(), lineText.length());
            if (span.start() >= end) {
                continue;
            }
            TextStyle style = span.style();
            float origin = row.offsetOf(i, flow);
            float x = base + origin;
            float width = !row.isPositioned() && i == spans.size() - 1
                    ? Math.max(0.0F, row.width() - origin)
                    : measurer.width(lineText, span.start(), end, style);
            flow = origin + width;
            lastStyle = style;
            cursorX = x + width;

            if (x + width < ctx.textLeft()) {
                continue;
            }
            if (x > right) {
                break;
            }

            String text = lineText.substring(span.start(), end);

            int color = resolveColor(ctx, boxStyle, style);

            if (ellipsis != null && x + width > limit) {
                String head = cutToWidth(measurer, text, style, limit - x);
                drawRun(graphics, boxStyle, measurer, head, style, x, rowBottom, color);
                cursorX = x + measurer.width(head, style);
                truncated = true;
                break;
            }
            if (x < ctx.textLeft() || x + width > right) {
                VisibleRun clipped = clipRun(measurer, style, text, x, ctx.textLeft(), right);
                drawRun(graphics, boxStyle, measurer, clipped.text(), style, clipped.x(), rowBottom, color);
            } else {
                drawRun(graphics, boxStyle, measurer, text, style, x, rowBottom, color);
            }
        }

        if (ellipsis != null) {
            drawRun(graphics, boxStyle, measurer, ellipsis, lastStyle, truncated ? cursorX : Math.min(cursorX, limit),
                    rowBottom, resolveColor(ctx, boxStyle, lastStyle));
        }
    }

    protected VisibleRun clipRun(TextMeasurer measurer, TextStyle style, String text, float x, float left,
                                float right) {
        String visible = text;
        float startX = x;

        float skipped = left - x;
        if (skipped > 0.0F) {
            int head = TextMeasurer.snapToCodePoint(text, measurer.fitCharacters(text, style, skipped));
            if (head > 0) {
                startX = x + measurer.width(text, 0, head, style);
                visible = text.substring(head);
            }
        }

        float available = right - startX;
        if (available <= 0.0F) {
            return new VisibleRun("", startX);
        }
        int fits = measurer.fitCharacters(visible, style, available);
        if (fits < visible.length()) {
            int end = TextMeasurer.snapToCodePoint(visible, Math.min(fits + CLIP_LOOKAHEAD, visible.length()));
            visible = visible.substring(0, end);
        }
        return new VisibleRun(visible, startX);
    }

    protected void drawRun(DLGuiGraphics graphics, TextBoxStyle boxStyle, TextMeasurer measurer, String text,
                           TextStyle style, float x, int rowBottom, int color) {
        if (text.isEmpty()) {
            return;
        }
        float height = measurer.lineHeight(style);
        float textY = rowBottom - height;
        boolean decorated = style.flags() != 0;
        float width = decorated || style.hasBackground() ? measurer.width(text, style) : 0.0F;

        if (style.hasBackground()) {
            graphics.graphics().fill(Math.round(x) - 1, Math.round(textY) - 1,
                    Math.round(x + width) + 1, rowBottom, style.backgroundColor());
        }

        PoseStack pose = graphics.poseStack();
        pose.pushPose();
        pose.translate(x, textY, 0.0F);
        pose.scale(style.scale(), style.scale(), 1.0F);
        graphics.graphics().drawString(measurer.font(), Component.literal(text).setStyle(style.toVanillaStyle()),
                0, 0, color, style.shadow());
        pose.popPose();

        if (decorated) {
            decorateRun(graphics, boxStyle, measurer, text, style, x, textY, width, height, color);
        }
    }

    protected void decorateRun(DLGuiGraphics graphics, TextBoxStyle boxStyle, TextMeasurer measurer, String text,
                               TextStyle style, float x, float y, float width, float height, int color) {
        if (style.flags() == 0) {
            return;
        }
        SpanDecoration decoration = new SpanDecoration(graphics, style, text, x, y, width, height, color, boxStyle,
                measurer);
        for (StyleFlag flag : StyleFlag.all()) {
            if (flag.isSet(style.flags())) {
                flag.effect().render(decoration);
            }
        }
    }

    protected static String cutToWidth(TextMeasurer measurer, String text, TextStyle style, float available) {
        if (available <= 0.0F) {
            return "";
        }
        int fits = TextMeasurer.snapToCodePoint(text, measurer.fitCharacters(text, style, available));
        return text.substring(0, Math.max(0, Math.min(fits, text.length())));
    }

    protected int resolveColor(TextBoxRenderContext ctx, TextBoxStyle boxStyle, TextStyle style) {
        if (!ctx.enabled()) {
            return boxStyle.textDisabled;
        }
        if (style.hasLink() && style.link().equals(ctx.hoveredLink())) {
            return boxStyle.linkHovered;
        }

        int color = style.color();
        if (style.flags() == 0) {
            return color;
        }
        for (StyleFlag flag : StyleFlag.all()) {
            if (flag.isSet(style.flags())) {
                color = flag.effect().color(color, boxStyle);
            }
        }
        return color;
    }

    protected void renderCaret(DLGuiGraphics graphics, TextBoxRenderContext ctx, VisualRow row, String lineText,
                               int lineStart, int top, int bottom, TextBoxStyle boxStyle, TextMeasurer measurer) {
        if (!ctx.focused() || !ctx.caretVisible() || !ctx.enabled()) {
            return;
        }
        int column = ctx.caretOffset() - lineStart;
        boolean inRow = column >= row.startColumn()
                && (column < row.endColumn() || column == row.endColumn() && isLastRowColumn(row, lineText, column));
        if (!inRow) {
            return;
        }

        float x = TextGeometry.xOfColumn(measurer, row, lineText, column);
        int caretX = (int) Math.round(ctx.textLeft() - ctx.scrollX() + x);
        if (boxStyle.invertCaret) {
            graphics.graphics().fill(RenderType.guiTextHighlight(), caretX, top, caretX + boxStyle.caretWidth, bottom, INVERTED_COLOR);
        } else {
            graphics.graphics().fill(caretX, top, caretX + boxStyle.caretWidth, bottom, boxStyle.caret);
        }
    }

    private static boolean isLastRowColumn(VisualRow row, String lineText, int column) {
        return column >= lineText.length() || row.endColumn() >= lineText.length();
    }

    protected void renderGutter(DLGuiGraphics graphics, TextBoxRenderContext ctx, int line, float lineTop,
                                boolean isCaretLine, TextBoxStyle boxStyle, TextMeasurer measurer) {
        String label = Integer.toString(line + 1);
        int width = measurer.font().width(label);
        int x = ctx.textLeft() - boxStyle.gutterPadding - width;
        int y = Math.round(ctx.textTop() + lineTop
                + layout.layoutOf(line).row(0).baseline() - measurer.baseLineHeight());
        graphics.graphics().drawString(measurer.font(), label, x, y,
                isCaretLine ? boxStyle.gutterTextActive : boxStyle.gutterText, false);
    }

    public StyledSpan spanAt(TextBoxRenderContext ctx, double localX, double localY) {
        int line = layout.heights().lineAtOffset((float) (localY - ctx.textTop() + ctx.scrollY()));
        LineLayout lineLayout = layout.layoutOf(line);
        String lineText = layout.textOf(line);

        float rowTop = ctx.textTop() + layout.heights().offsetOf(line) - (float) ctx.scrollY();
        for (VisualRow row : lineLayout.rows()) {
            if (localY >= rowTop && localY < rowTop + row.height()) {
                float x = (float) (localX - ctx.textLeft() + ctx.scrollX());
                int column = TextGeometry.columnAtX(layout.measurer(), row, lineText, x);
                return TextGeometry.spanAtColumn(row, column);
            }
            rowTop += row.height();
        }
        return null;
    }

    public String linkAt(TextBoxRenderContext ctx, double localX, double localY) {
        StyledSpan span = spanAt(ctx, localX, localY);
        return span != null && span.style().hasLink() ? span.style().link() : null;
    }

    public BlockKind blockKindOf(int line) {
        return layout.layoutOf(line).parsed().kind();
    }
}
