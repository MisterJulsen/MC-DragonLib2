package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextRange;

public final class Caret {

    private int offset;
    private int anchor;

    private float goalX = -1.0F;

    public int offset() {
        return offset;
    }

    public int anchor() {
        return anchor;
    }

    public TextRange selection() {
        return TextRange.of(anchor, offset);
    }

    public boolean hasSelection() {
        return anchor != offset;
    }

    public void moveTo(int offset, boolean extend) {
        this.offset = offset;
        if (!extend) {
            this.anchor = offset;
        }
        this.goalX = -1.0F;
    }

    public void moveVertically(int offset, boolean extend, float goalX) {
        this.offset = offset;
        if (!extend) {
            this.anchor = offset;
        }
        this.goalX = goalX;
    }

    public void select(TextRange range) {
        this.anchor = range.start();
        this.offset = range.end();
        this.goalX = -1.0F;
    }

    public void clearSelection() {
        this.anchor = offset;
    }

    public float goalX() {
        return goalX;
    }

    public void setGoalX(float goalX) {
        this.goalX = goalX;
    }

    public void clamp(int length) {
        this.offset = Math.max(0, Math.min(offset, length));
        this.anchor = Math.max(0, Math.min(anchor, length));
    }
}
