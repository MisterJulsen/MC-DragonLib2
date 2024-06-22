package de.mrjulsen.mcdragonlib.client.gui.widgets;

import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public abstract class DLRenderable implements Renderable, ITickable, IDragonLibWidget {

    protected boolean active;
    protected boolean visible;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    
    public DLRenderable(int x, int y, int width, int height) {
    }

    @Override
    public void set_visible(boolean b) {
        this.visible = b;
    }

    @Override
    public boolean visible() {
        return visible;
    }

    @Override
    public void set_active(boolean b) {
        this.active = b;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void onFocusChangeEvent(boolean focus) {}

    @Override
    public DLContextMenu getContextMenu() {
        return null;
    }

    @Override
    public void setMenu(DLContextMenu menu) {}

    @Override
    public boolean isMouseSelected() {
        return false;
    }

    @Override
    public void setMouseSelected(boolean selected) { }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void set_x(int x) {
        this.x = x;
    }

    @Override
    public void set_y(int y) {
        this.y = y;
    }

    @Override
    public void set_width(int w) {
        this.width = w;
    }

    @Override
    public void set_height(int h) {
        this.height = h;
    }

    @Override
    public int getContextMenuOpenButton() {
        return IDragonLibWidget.NO_CONTEXT_MENU_BUTTON;
    }

    @Override
    public boolean contextMenuMouseClickHandler(int mouseX, int mouseY, int button, int xOffset, int yOffset, GuiAreaDefinition openingBounds) {
        return false;
    }

    @Override
    public final void render(GuiGraphics var1, int var2, int var3, float var4) { }
}
