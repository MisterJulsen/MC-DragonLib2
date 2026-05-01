package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext;

import java.util.function.Consumer;

public class LineSplitter implements TextSegment.CharCallback {

    public record LineBreak(int breakIndex, float lineWidth, float scale, float lineHeight) {}

    private final WidthProvider widthProvider;
    private final float maxWidth;
    public int offset;
    private final Consumer<LineBreak> onLineBreak;

    private float currentLineWidth = 0f;
    private float widthSinceLastBreak = 0f;
    private int lastSpaceGlobalIndex = -1;
    private float widthSinceLastSpace = 0f;
    private float scale = 1f;
    private float lineHeight = 1f;

    public LineSplitter(float maxWidth, int offset, WidthProvider widthProvider, Consumer<LineBreak> onLineBreak) {
        this.widthProvider = widthProvider;
        this.maxWidth = Math.max(maxWidth, 1f);
        this.offset = offset;
        this.onLineBreak = onLineBreak;
    }

    @Override
    public boolean accept(int index, TextStyle style, int c) {
        int globalIndex = index + offset;
        if (c == '\n') {
            emitBreak(globalIndex);
            scale = Math.max(scale, style.scale());
            lineHeight = Math.max(lineHeight, style.scale() * style.font().lineHeight);
            return true;
        }

        float glyphWidth = widthProvider.getWidth(style, c);
        currentLineWidth += glyphWidth;
        widthSinceLastBreak += glyphWidth;
        widthSinceLastSpace += glyphWidth;

        if (c == ' ') {
            lastSpaceGlobalIndex = globalIndex;
            widthSinceLastSpace = 0f;
        }

        if (currentLineWidth > maxWidth) {
            if (lastSpaceGlobalIndex >= 0) {
                float nextLineWidth = widthSinceLastSpace;
                emitBreak(lastSpaceGlobalIndex);
                currentLineWidth = nextLineWidth;
                widthSinceLastBreak = nextLineWidth;
            } else {
                emitBreak(globalIndex);
                currentLineWidth = glyphWidth;
                widthSinceLastBreak = glyphWidth;
            }
        }

        scale = Math.max(scale, style.scale());
        lineHeight = Math.max(lineHeight, style.scale() * style.font().lineHeight);
        return true;
    }

    private void emitBreak(int breakAt) {
        onLineBreak.accept(new LineBreak(breakAt, widthSinceLastBreak, scale, lineHeight));
        currentLineWidth = 0f;
        widthSinceLastBreak = 0f;
        lastSpaceGlobalIndex = -1;
        widthSinceLastSpace = 0f;
        scale = 1f;
        lineHeight = 1f;
    }

    public float highestScale() {
        return scale;
    }
    public float widthSinceLastBreak() {
        return widthSinceLastBreak;
    }
    public float lineHeight() {
        return lineHeight;
    }
}