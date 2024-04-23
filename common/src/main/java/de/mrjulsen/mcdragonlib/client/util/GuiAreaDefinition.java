package de.mrjulsen.mcdragonlib.client.util;

import de.mrjulsen.mcdragonlib.client.gui.widgets.WidgetContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public class GuiAreaDefinition {
    private final int x, y, w, h;

    public GuiAreaDefinition(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public int getRight() {
        return x + w;
    }

    public int getBottom() {
        return y + h;
    }

    public int getLeft() {
        return this.getX();
    }

    public int getTop() {
        return this.getY();
    }

    public boolean isInBounds(double mouseX, double mouseY) {
        return mouseX >= getLeft() && mouseX < getRight() && mouseY >= getTop() && mouseY < getBottom();
    }

    public static GuiAreaDefinition empty() {
        return new GuiAreaDefinition(0, 0, 0, 0);
    }

    public static GuiAreaDefinition of(Screen screen) {
        return new GuiAreaDefinition(0, 0, screen.width, screen.height);
    }

    public static GuiAreaDefinition of(WidgetContainer container) {
        return new GuiAreaDefinition(container.getX(), container.getY(), container.getWidth(), container.getHeight());
    }

    public static GuiAreaDefinition of(AbstractWidget widget) {
        return new GuiAreaDefinition(widget.x, widget.y, widget.getWidth(), widget.getHeight());
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s, %s]", getX(), getY(), getRight(), getBottom());
    }
}