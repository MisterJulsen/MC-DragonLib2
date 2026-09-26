package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document;

public record TextPosition(int line, int column) implements Comparable<TextPosition> {

    public static final TextPosition ZERO = new TextPosition(0, 0);

    @Override
    public int compareTo(TextPosition other) {
        int byLine = Integer.compare(line, other.line);
        return byLine != 0 ? byLine : Integer.compare(column, other.column);
    }
}
