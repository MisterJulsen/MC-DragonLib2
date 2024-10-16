package de.mrjulsen.mcdragonlib.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import net.minecraft.client.gui.components.Widget;

public abstract class DLRenderable implements Widget, ITickable, IDragonLibWidget {

    protected boolean active = true;
    protected boolean visible = true;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    
    public DLRenderable(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
    public boolean active() {
        return isActive();
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
    public final void render(PoseStack var1, int var2, int var3, float var4) {
        renderMainLayer(new Graphics(var1), var2, var3, var4);
    }

    @Override
    public void tick() { }
}
