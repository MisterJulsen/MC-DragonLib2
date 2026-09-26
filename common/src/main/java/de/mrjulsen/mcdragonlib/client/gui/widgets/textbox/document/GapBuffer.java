package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document;

public final class GapBuffer implements CharSequence {

    private static final int MIN_GAP = 256;
    private static final float GROWTH_FACTOR = 1.5F;
    private char[] buffer;
    private int gapStart;
    private int gapEnd;

    public GapBuffer() {
        this(MIN_GAP);
    }

    public GapBuffer(int capacity) {
        this.buffer = new char[Math.max(capacity, MIN_GAP)];
        this.gapStart = 0;
        this.gapEnd = this.buffer.length;
    }

    public GapBuffer(CharSequence initialContent) {
        this(initialContent.length() + MIN_GAP);
        insert(0, initialContent);
    }

    @Override
    public int length() {
        return buffer.length - gapLength();
    }

    private int gapLength() {
        return gapEnd - gapStart;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length());
        }
        return index < gapStart ? buffer[index] : buffer[index + gapLength()];
    }

    private void moveGapTo(int index) {
        if (index == gapStart) {
            return;
        }
        if (index < gapStart) {
            int count = gapStart - index;
            System.arraycopy(buffer, index, buffer, gapEnd - count, count);
            gapStart -= count;
            gapEnd -= count;
        } else {
            int count = index - gapStart;
            System.arraycopy(buffer, gapEnd, buffer, gapStart, count);
            gapStart += count;
            gapEnd += count;
        }
    }

    private void ensureGap(int required) {
        if (gapLength() >= required) {
            return;
        }
        int contentLength = length();
        int newCapacity = Math.max((int) ((contentLength + required) * GROWTH_FACTOR), contentLength + required + MIN_GAP);
        char[] next = new char[newCapacity];

        int tailLength = buffer.length - gapEnd;
        System.arraycopy(buffer, 0, next, 0, gapStart);
        System.arraycopy(buffer, gapEnd, next, newCapacity - tailLength, tailLength);

        this.buffer = next;
        this.gapEnd = newCapacity - tailLength;
    }

    public void insert(int index, CharSequence text) {
        int count = text.length();
        if (count == 0) {
            return;
        }
        checkPosition(index);
        moveGapTo(index);
        ensureGap(count);

        if (text instanceof String s) {
            s.getChars(0, count, buffer, gapStart);
        } else {
            for (int i = 0; i < count; i++) {
                buffer[gapStart + i] = text.charAt(i);
            }
        }
        gapStart += count;
    }

    public void delete(int start, int end) {
        if (start == end) {
            return;
        }
        checkRange(start, end);

        moveGapTo(end);
        gapStart -= (end - start);
    }

    public void getChars(int start, int end, char[] dest, int destOffset) {
        checkRange(start, end);
        if (end <= gapStart) {
            System.arraycopy(buffer, start, dest, destOffset, end - start);
        } else if (start >= gapStart) {
            System.arraycopy(buffer, start + gapLength(), dest, destOffset, end - start);
        } else {
            int head = gapStart - start;
            System.arraycopy(buffer, start, dest, destOffset, head);
            System.arraycopy(buffer, gapEnd, dest, destOffset + head, end - gapStart);
        }
    }

    public String substring(int start, int end) {
        checkRange(start, end);
        char[] dest = new char[end - start];
        getChars(start, end, dest, 0);
        return new String(dest);
    }

    public int indexOf(char c, int start, int end) {
        for (int i = start; i < end; i++) {
            if (charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    @Override
    public String toString() {
        return substring(0, length());
    }

    private void checkPosition(int index) {
        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException("Position " + index + " out of bounds for length " + length());
        }
    }

    private void checkRange(int start, int end) {
        if (start < 0 || end > length() || start > end) {
            throw new IndexOutOfBoundsException("Range [" + start + ", " + end + ") out of bounds for length " + length());
        }
    }
}
