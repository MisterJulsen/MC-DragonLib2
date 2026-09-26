package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document;

import java.util.ArrayDeque;
import java.util.Deque;

public final class UndoManager {

    public static final long COALESCE_WINDOW_MS = 400L;

    public static final int DEFAULT_HISTORY_LIMIT = 512;

    private final Deque<DocumentEdit> undoStack = new ArrayDeque<>();
    private final Deque<DocumentEdit> redoStack = new ArrayDeque<>();
    private final int historyLimit;

    private boolean suspended;

    public UndoManager() {
        this(DEFAULT_HISTORY_LIMIT);
    }

    public UndoManager(int historyLimit) {
        this.historyLimit = Math.max(1, historyLimit);
    }

    public void record(DocumentEdit edit) {
        if (suspended) {
            return;
        }
        redoStack.clear();

        DocumentEdit previous = undoStack.peek();
        if (previous != null && canCoalesce(previous, edit)) {
            undoStack.pop();
            undoStack.push(merge(previous, edit));
            return;
        }

        undoStack.push(edit);
        while (undoStack.size() > historyLimit) {
            undoStack.removeLast();
        }
    }

    private static boolean canCoalesce(DocumentEdit previous, DocumentEdit next) {
        if (next.timestamp() - previous.timestamp() > COALESCE_WINDOW_MS) {
            return false;
        }
        boolean bothInserts = previous.removedText().isEmpty() && next.removedText().isEmpty();
        boolean bothDeletes = previous.insertedText().isEmpty() && next.insertedText().isEmpty();

        if (bothInserts) {
            return next.start() == previous.endAfter() && !next.insertedText().contains("\n");
        }
        if (bothDeletes) {
            return next.endBefore() == previous.start();
        }
        return false;
    }

    private static DocumentEdit merge(DocumentEdit previous, DocumentEdit next) {
        if (next.removedText().isEmpty()) {
            return new DocumentEdit(
                    previous.start(),
                    previous.removedText(),
                    previous.insertedText() + next.insertedText(),
                    previous.caretBefore(),
                    next.caretAfter(),
                    next.timestamp()
            );
        }
        return new DocumentEdit(
                next.start(),
                next.removedText() + previous.removedText(),
                "",
                previous.caretBefore(),
                next.caretAfter(),
                next.timestamp()
        );
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public DocumentEdit popUndo() {
        if (undoStack.isEmpty()) {
            return null;
        }
        DocumentEdit edit = undoStack.pop();
        redoStack.push(edit);
        return edit;
    }

    public DocumentEdit popRedo() {
        if (redoStack.isEmpty()) {
            return null;
        }
        DocumentEdit edit = redoStack.pop();
        undoStack.push(edit);
        return edit;
    }

    public void withoutRecording(Runnable action) {
        boolean previous = suspended;
        suspended = true;
        try {
            action.run();
        } finally {
            suspended = previous;
        }
    }

    public void breakCoalescing() {
        DocumentEdit head = undoStack.peek();
        if (head != null) {
            undoStack.pop();
            undoStack.push(new DocumentEdit(head.start(), head.removedText(), head.insertedText(), head.caretBefore(), head.caretAfter(), Long.MIN_VALUE / 2));
        }
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
