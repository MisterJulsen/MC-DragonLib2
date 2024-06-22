package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class DLButton extends Button implements IDragonLibWidget {
    
    private DLContextMenu menu;
    private boolean mouseSelected;
    protected final Font font;
    protected AreaStyle style = AreaStyle.NATIVE;
    
    protected int fontColor = 0xFFFFFFFF;
    protected int backColor = 0xFFFFFFFF;

    @SuppressWarnings({ "unchecked", "resource" })
    public <T extends DLButton> DLButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Consumer<T> pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, (btn) -> pOnPress.accept((T)btn));
        this.font = Minecraft.getInstance().font;
    }

    public DLButton(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        this(pX, pY, pWidth, pHeight, pMessage, (btn) -> {});
    }

    public void setRenderStyle(AreaStyle style) {
        this.style = style;
        if (style.isCustom()) {
            setBackColor(DragonLib.DEFAULT_BUTTON_COLOR);
        }
    }

    public AreaStyle getStyle() {
        return style;
    }

    @Override
    public void setMenu(DLContextMenu menu) {
        this.menu = menu;
    }

    public void onHoverChange(int mouseX, int mouseY, boolean isHovering) {}

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderMainLayer(new Graphics(poseStack), mouseX, mouseY, partialTicks);
    }

    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        DynamicGuiRenderer.renderArea(graphics, x, y, width, height, getBackColor(), style, isActive() ? (isFocused() || isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON) : ButtonState.DISABLED);
        int j = active ? getFontColor() : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED;
        GuiUtils.drawString(graphics, font, this.x + this.width / 2, this.y + (this.height - 8) / 2, this.getMessage(), j, EAlignment.CENTER, true);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        IDragonLibWidget.super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);

        boolean wasHovering = isHovered;
        setHovered(mouseX, mouseY);
        if (wasHovering != isHovered) {
            onHoverChange(mouseX, mouseY, isHovered);
        }
    }

    public boolean setHovered(int mouseX, int mouseY) {
        return isHovered = mouseX >= x && mouseX < x + getWidth() && mouseY >= y && mouseY < y + getHeight();
    }

    @Override
    public DLContextMenu getContextMenu() {
        return menu;
    }

    @Override
    public void onFocusChangeEvent(boolean focus) {
        setFocused(focus);
    }

    @Override
    public boolean isMouseSelected() {
        return mouseSelected;
    }

    @Override
    public void setMouseSelected(boolean selected) {
        this.mouseSelected = selected;
    }

    public int getBackColor() {
        return backColor;
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setBackColor(int color) {
        this.backColor = color;
    }

    public void setFontColor(int color) {
        this.fontColor = color;        
    }

    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
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
}
