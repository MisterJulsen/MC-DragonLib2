package de.mrjulsen.mcdragonlib.client.gui.v2.text.render;

public interface IHighlightSource {

    @FunctionalInterface
    interface IHighlightVisitor {
        void visit(int start, int end, boolean active);
    }

    void forEachHighlight(int from, int to, IHighlightVisitor visitor);
}
