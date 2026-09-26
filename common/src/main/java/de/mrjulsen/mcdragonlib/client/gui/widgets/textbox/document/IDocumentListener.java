package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document;

@FunctionalInterface
public interface IDocumentListener {
    void onDocumentChanged(TextDocument document, DocumentEdit edit, int firstLine, int removedLines, int insertedLines);
}
