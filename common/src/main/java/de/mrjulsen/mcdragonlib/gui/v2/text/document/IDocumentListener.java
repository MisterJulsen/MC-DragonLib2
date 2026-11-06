package de.mrjulsen.mcdragonlib.gui.v2.text.document;

@FunctionalInterface
public interface IDocumentListener {
    void onDocumentChanged(TextDocument document, DocumentEdit edit, int firstLine, int removedLines, int insertedLines);
}
