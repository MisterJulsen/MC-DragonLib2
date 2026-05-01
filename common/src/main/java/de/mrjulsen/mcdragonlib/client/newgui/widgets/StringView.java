package de.mrjulsen.mcdragonlib.client.newgui.widgets;

public record StringView(int beginIndex, int endIndex) {
    static final StringView EMPTY = new StringView(0, 0);

    public int length() {
        return this.endIndex - this.beginIndex;
    }

    public boolean isEmpty() {
        return endIndex() <= beginIndex();
    }
}