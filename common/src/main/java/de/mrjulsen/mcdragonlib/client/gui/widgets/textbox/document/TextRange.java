package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document;

public record TextRange(int start, int end) {

    public static final TextRange EMPTY = new TextRange(0, 0);

    public TextRange {
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
    }

    public static TextRange of(int a, int b) {
        return new TextRange(a, b);
    }

    public static TextRange at(int offset) {
        return new TextRange(offset, offset);
    }

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return start == end;
    }

    public boolean contains(int offset) {
        return offset >= start && offset < end;
    }

    public boolean intersects(TextRange other) {
        return start < other.end && other.start < end;
    }

    public TextRange intersection(TextRange other) {
        int s = Math.max(start, other.start);
        int e = Math.min(end, other.end);
        return s < e ? new TextRange(s, e) : TextRange.at(start);
    }

    public TextRange shift(int delta) {
        return new TextRange(start + delta, end + delta);
    }

    public TextRange clamp(int limit) {
        return new TextRange(Math.max(0, Math.min(start, limit)), Math.max(0, Math.min(end, limit)));
    }
}
