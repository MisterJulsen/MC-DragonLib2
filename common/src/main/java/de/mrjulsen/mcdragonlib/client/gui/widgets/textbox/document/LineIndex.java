package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document;

import java.util.Arrays;

public final class LineIndex {

    private int[] lineStarts = new int[] { 0 };
    private int lineCount = 1;

    public void rebuild(CharSequence text) {
        int length = text.length();
        int newlines = 0;
        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == '\n') {
                newlines++;
            }
        }

        int[] starts = new int[newlines + 1];
        starts[0] = 0;
        int index = 1;
        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == '\n') {
                starts[index++] = i + 1;
            }
        }
        this.lineStarts = starts;
        this.lineCount = starts.length;
    }

    public int lineCount() {
        return lineCount;
    }

    public int lineStart(int line) {
        return lineStarts[clampLine(line)];
    }

    public int lineEnd(int line, int documentLength) {
        line = clampLine(line);
        return line + 1 < lineCount ? lineStarts[line + 1] - 1 : documentLength;
    }

    public int lineOfOffset(int offset) {
        int index = Arrays.binarySearch(lineStarts, 0, lineCount, offset);

        return index >= 0 ? index : -index - 2;
    }

    public void applyEdit(int start, int end, CharSequence insertedText) {
        int firstLine = lineOfOffset(start);
        int lastLine = lineOfOffset(end);

        int removedCount = lastLine - firstLine;

        int insertedLength = insertedText.length();
        int insertedNewlines = 0;
        for (int i = 0; i < insertedLength; i++) {
            if (insertedText.charAt(i) == '\n') {
                insertedNewlines++;
            }
        }

        int newLineCount = lineCount - removedCount + insertedNewlines;
        int[] target = lineStarts.length >= newLineCount ? lineStarts : new int[Math.max(newLineCount, lineCount * 2)];

        int tailStart = lastLine + 1;
        int tailLength = lineCount - tailStart;
        int newTailStart = firstLine + 1 + insertedNewlines;
        int delta = insertedLength - (end - start);

        if (target != lineStarts) {
            System.arraycopy(lineStarts, 0, target, 0, firstLine + 1);
        }

        System.arraycopy(lineStarts, tailStart, target, newTailStart, tailLength);
        for (int i = 0; i < tailLength; i++) {
            target[newTailStart + i] += delta;
        }

        int cursor = firstLine + 1;
        for (int i = 0; i < insertedLength; i++) {
            if (insertedText.charAt(i) == '\n') {
                target[cursor++] = start + i + 1;
            }
        }

        this.lineStarts = target;
        this.lineCount = newLineCount;
    }

    private int clampLine(int line) {
        if (line < 0) {
            return 0;
        }
        return line >= lineCount ? lineCount - 1 : line;
    }
}
