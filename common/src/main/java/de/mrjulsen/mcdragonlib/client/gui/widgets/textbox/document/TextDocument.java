package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

public final class TextDocument {

    private final GapBuffer buffer;
    private final LineIndex lineIndex = new LineIndex();
    private final UndoManager undoManager;
    private final List<IDocumentListener> listeners = new ArrayList<>(2);

    private long generation;

    private boolean readOnly;
    private IntPredicate wordSeparator = TextDocument::isDefaultWordSeparator;

    public TextDocument() {
        this("");
    }

    public TextDocument(String initialText) {
        this(initialText, new UndoManager());
    }

    public TextDocument(String initialText, UndoManager undoManager) {
        String normalized = normalizeLineEndings(initialText);
        this.buffer = new GapBuffer(normalized);
        this.undoManager = undoManager;
        this.lineIndex.rebuild(this.buffer);
    }

    public static String normalizeLineEndings(String text) {
        if (text.indexOf('\r') < 0) {
            return text;
        }
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    public int length() {
        return buffer.length();
    }

    public int lineCount() {
        return lineIndex.lineCount();
    }

    public long generation() {
        return generation;
    }

    public char charAt(int offset) {
        return buffer.charAt(offset);
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public CharSequence characters() {
        return buffer;
    }

    public String getText() {
        return buffer.toString();
    }

    public String getText(TextRange range) {
        TextRange clamped = range.clamp(length());
        return buffer.substring(clamped.start(), clamped.end());
    }

    public int lineStart(int line) {
        return lineIndex.lineStart(line);
    }

    public int lineEnd(int line) {
        return lineIndex.lineEnd(line, length());
    }

    public int lineLength(int line) {
        return lineEnd(line) - lineStart(line);
    }

    public String getLine(int line) {
        return buffer.substring(lineStart(line), lineEnd(line));
    }

    public int lineOfOffset(int offset) {
        return lineIndex.lineOfOffset(clampOffset(offset));
    }

    public TextPosition positionOf(int offset) {
        int clamped = clampOffset(offset);
        int line = lineIndex.lineOfOffset(clamped);
        return new TextPosition(line, clamped - lineStart(line));
    }

    public int offsetOf(TextPosition position) {
        int line = Math.max(0, Math.min(position.line(), lineCount() - 1));
        int start = lineStart(line);
        return Math.min(start + Math.max(0, position.column()), lineEnd(line));
    }

    public int clampOffset(int offset) {
        return Math.max(0, Math.min(offset, length()));
    }

    public boolean isInsideCodePoint(int offset) {
        return offset > 0 && offset < length() && Character.isLowSurrogate(charAt(offset)) && Character.isHighSurrogate(charAt(offset - 1));
    }

    public int startOfCodePoint(int offset) {
        int i = clampOffset(offset);
        return isInsideCodePoint(i) ? i - 1 : i;
    }

    public int endOfCodePoint(int offset) {
        int i = clampOffset(offset);
        return isInsideCodePoint(i) ? i + 1 : i;
    }

    public int offsetBefore(int offset) {
        int i = startOfCodePoint(offset);
        if (i <= 0) {
            return 0;
        }
        if (i >= 2 && Character.isLowSurrogate(charAt(i - 1)) && Character.isHighSurrogate(charAt(i - 2))) {
            return i - 2;
        }
        return i - 1;
    }

    public int offsetAfter(int offset) {
        int i = startOfCodePoint(offset);
        int limit = length();
        if (i >= limit) {
            return limit;
        }
        if (Character.isHighSurrogate(charAt(i)) && i + 1 < limit && Character.isLowSurrogate(charAt(i + 1))) {
            return i + 2;
        }
        return i + 1;
    }

    public int codePointAt(int offset) {
        int i = startOfCodePoint(offset);
        if (i >= length()) {
            return -1;
        }
        char c = charAt(i);
        if (Character.isHighSurrogate(c) && i + 1 < length() && Character.isLowSurrogate(charAt(i + 1))) {
            return Character.toCodePoint(c, charAt(i + 1));
        }
        return c;
    }

    public TextRange snapToCodePoints(TextRange range) {
        TextRange clamped = range.clamp(length());
        return new TextRange(startOfCodePoint(clamped.start()), endOfCodePoint(clamped.end()));
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public UndoManager undo() {
        return undoManager;
    }

    public DocumentEdit replace(int start, int end, String text, int caretBefore, int caretAfter) {
        if (readOnly) {
            return null;
        }
        int s = clampOffset(Math.min(start, end));
        int e = clampOffset(Math.max(start, end));
        String inserted = normalizeLineEndings(text);

        if (s == e && inserted.isEmpty()) {
            return null;
        }

        String removed = buffer.substring(s, e);
        if (removed.equals(inserted)) {
            return null;
        }

        DocumentEdit edit = new DocumentEdit(s, removed, inserted, caretBefore, caretAfter, System.currentTimeMillis());
        undoManager.record(edit);
        applyInternal(edit);
        return edit;
    }

    public DocumentEdit replace(int start, int end, String text) {
        int s = Math.min(start, end);
        return replace(start, end, text, s, s + normalizeLineEndings(text).length());
    }

    public DocumentEdit insert(int offset, String text) {
        return replace(offset, offset, text);
    }

    public DocumentEdit delete(TextRange range) {
        return replace(range.start(), range.end(), "");
    }

    public void setText(String text) {
        String normalized = normalizeLineEndings(text);
        undoManager.clear();
        undoManager.withoutRecording(() -> {
            DocumentEdit edit = new DocumentEdit(0, buffer.substring(0, buffer.length()), normalized, 0, normalized.length(), System.currentTimeMillis());
            applyInternal(edit);
        });
    }

    public int undoLastEdit() {
        if (readOnly) {
            return -1;
        }
        DocumentEdit edit = undoManager.popUndo();
        if (edit == null) {
            return -1;
        }
        undoManager.withoutRecording(() -> applyInternal(edit.inverted()));
        return edit.caretBefore();
    }

    public int redoLastEdit() {
        if (readOnly) {
            return -1;
        }
        DocumentEdit edit = undoManager.popRedo();
        if (edit == null) {
            return -1;
        }
        undoManager.withoutRecording(() -> applyInternal(edit));
        return edit.caretAfter();
    }

    private void applyInternal(DocumentEdit edit) {
        int start = edit.start();
        int end = edit.endBefore();

        int firstLine = lineIndex.lineOfOffset(start);
        int lastLine = lineIndex.lineOfOffset(end);
        int removedLines = lastLine - firstLine;

        int insertedLines = 0;
        String insertedText = edit.insertedText();
        for (int i = 0; i < insertedText.length(); i++) {
            if (insertedText.charAt(i) == '\n') {
                insertedLines++;
            }
        }

        buffer.delete(start, end);
        buffer.insert(start, insertedText);
        lineIndex.applyEdit(start, end, insertedText);
        generation++;

        for (IDocumentListener listener : listeners) {
            listener.onDocumentChanged(this, edit, firstLine, removedLines, insertedLines);
        }
    }

    public void addListener(IDocumentListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IDocumentListener listener) {
        listeners.remove(listener);
    }

    public int previousWordBoundary(int offset) {
        int i = startOfCodePoint(offset);
        while (i > 0 && isWordSeparator(charAt(i - 1)) && charAt(i - 1) != '\n') {
            i--;
        }
        while (i > 0 && !isWordSeparator(charAt(i - 1))) {
            i--;
        }
        return i;
    }

    public int nextWordBoundary(int offset) {
        int limit = length();
        int i = startOfCodePoint(offset);
        while (i < limit && !isWordSeparator(charAt(i))) {
            i++;
        }
        while (i < limit && isWordSeparator(charAt(i)) && charAt(i) != '\n') {
            i++;
        }
        return i;
    }

    public TextRange wordAt(int offset) {
        int limit = length();
        int i = startOfCodePoint(offset);
        if (limit == 0) {
            return TextRange.EMPTY;
        }
        if (i >= limit || isWordSeparator(charAt(i))) {
            if (i == 0 || isWordSeparator(charAt(i - 1))) {
                int after = offsetAfter(i);
                return after - i > 1 ? new TextRange(i, after) : TextRange.at(i);
            }
            i = offsetBefore(i);
        }
        int start = i;
        while (start > 0 && !isWordSeparator(charAt(start - 1))) {
            start--;
        }
        int end = i;
        while (end < limit && !isWordSeparator(charAt(end))) {
            end++;
        }
        return snapToCodePoints(new TextRange(start, end));
    }

    public TextRange lineRangeWithBreak(int line) {
        int start = lineStart(line);
        int end = line + 1 < lineCount() ? lineStart(line + 1) : length();
        return new TextRange(start, end);
    }

    public IntPredicate wordSeparator() {
        return wordSeparator;
    }

    public void setWordSeparator(IntPredicate wordSeparator) {
        this.wordSeparator = wordSeparator == null ? TextDocument::isDefaultWordSeparator : wordSeparator;
    }

    public static boolean isDefaultWordSeparator(int codePoint) {
        return !Character.isLetterOrDigit(codePoint) && codePoint != '_';
    }

    private boolean isWordSeparator(char c) {
        return wordSeparator.test(c);
    }
}
