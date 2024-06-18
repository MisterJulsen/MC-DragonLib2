package de.mrjulsen.mcdragonlib.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import net.minecraft.client.gui.components.Widget;

public abstract class DLRenderable implements Widget, ITickable, IDragonLibWidget {

    protected boolean active;
    protected boolean visible;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    
    public DLRenderable(int x, int y, int width, int height) {
    }

    @Override
    public void setVisible(boolean b) {
        this.visible = b;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setActive(boolean b) {
        this.active = b;
    }

    @Override
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
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setWidth(int w) {
        this.width = w;
    }

    @Override
    public void setHeight(int h) {
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
    public final void render(PoseStack var1, int var2, int var3, float var4) { }
}
