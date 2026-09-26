package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render;

import java.util.function.IntPredicate;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextRange;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public record TextBoxRenderContext(
        int textLeft,
        int textTop,
        int textWidth,
        int textHeight,
        double scrollX,
        double scrollY,
        int caretOffset,
        TextRange selection,
        boolean focused,
        boolean enabled,
        boolean caretVisible,
        boolean highlightCurrentLine,
        String hoveredLink,
        IntPredicate lineHighlight,
        Rectangle gutterArea,
        IHighlightSource highlights,
        Rectangle clip,
        Rectangle textClip,
        int maxRows,
        String ellipsis
) {

    public TextBoxRenderContext(int textLeft, int textTop, int textWidth, int textHeight, double scrollX,
                                double scrollY, int caretOffset, TextRange selection, boolean focused,
                                boolean enabled, boolean caretVisible, boolean highlightCurrentLine,
                                String hoveredLink, IntPredicate lineHighlight, Rectangle gutterArea,
                                IHighlightSource highlights, Rectangle clip, Rectangle textClip) {
        this(textLeft, textTop, textWidth, textHeight, scrollX, scrollY, caretOffset, selection, focused, enabled,
                caretVisible, highlightCurrentLine, hoveredLink, lineHighlight, gutterArea, highlights, clip,
                textClip, 0, null);
    }

    public boolean hasRowLimit() {
        return maxRows > 0;
    }

    public boolean hasEllipsis() {
        return ellipsis != null && !ellipsis.isEmpty();
    }

    public boolean hasGutter() {
        return gutterArea != null;
    }

    public boolean hasHighlights() {
        return highlights != null;
    }

    public boolean hasTextClip() {
        return textClip != null && textClip.width() > 0 && textClip.height() > 0;
    }
}
