package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

public record TextContext(double localX, double localY, int line, int lineStart, String lineText, StyledSpan span, ParsedLine parsed) {

    public static final TextContext NONE = new TextContext(Double.NaN, Double.NaN, -1, 0, "", null, null);

    public boolean hasLine() {
        return line >= 0 && parsed != null;
    }

    public boolean hasSpan() {
        return span != null;
    }

    public BlockKind kind() {
        return parsed == null ? null : parsed.kind();
    }

    public boolean isKind(BlockKind kind) {
        return parsed != null && parsed.kind() == kind;
    }

    public String link() {
        return hasSpan() && span.style().hasLink() ? span.style().link() : null;
    }

    public boolean hasLink() {
        return link() != null;
    }

    public String tooltip() {
        return hasSpan() && span.style().hasTooltip() ? span.style().tooltip() : null;
    }

    public String meta() {
        return parsed == null ? null : parsed.meta();
    }

    public int offsetOf(int column) {
        return lineStart + Math.max(0, column);
    }
}
