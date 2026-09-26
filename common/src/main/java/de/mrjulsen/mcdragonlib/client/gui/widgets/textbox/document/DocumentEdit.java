package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document;

public record DocumentEdit(
        int start,
        String removedText,
        String insertedText,
        int caretBefore,
        int caretAfter,
        long timestamp
) {
    public int endBefore() {
        return start + removedText.length();
    }

    public int endAfter() {
        return start + insertedText.length();
    }

    public int lengthDelta() {
        return insertedText.length() - removedText.length();
    }

    public DocumentEdit inverted() {
        return new DocumentEdit(start, insertedText, removedText, caretAfter, caretBefore, timestamp);
    }
}
