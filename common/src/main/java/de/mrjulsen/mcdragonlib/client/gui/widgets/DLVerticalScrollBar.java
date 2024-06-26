package de.mrjulsen.mcdragonlib.client.gui.widgets;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;

public class DLVerticalScrollBar extends DLAbstractScrollBar<DLVerticalScrollBar> { 

    public DLVerticalScrollBar(int x, int y, int w, int h, GuiAreaDefinition scrollArea) {
        super(x, y, Math.max(MIN_SCROLLBAR_THICKNESS, w), Math.max(MIN_SCROLLBAR_THICKNESS, h), null);
    }

    public DLVerticalScrollBar(int x, int y, int h, GuiAreaDefinition scrollArea) {
        this(x, y, DEFAULT_SCROLLBAR_THICKNESS, h, scrollArea);
    }

    public DLVerticalScrollBar(int x, int y, int w, int h) {
        super(x, y, w, h, null);
    }

    public DLVerticalScrollBar(int x, int y, int h) {
        this(x, y, h, null);
    }

    @Override
    public void setRenderStyle(AreaStyle style) {
        switch (style) {
            case NATIVE -> this.style = AreaStyle.GRAY;
            default -> super.setRenderStyle(style);
        }
    }

    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Render background
        DynamicGuiRenderer.renderArea(graphics, getX(), getY(), width, height, getBackColor(), getStyle(), ButtonState.DOWN);

        // Render scrollbar
        int startU = canScroll() ? 20 : 25;
        int startV = 5;

        int x1 = getX() + 1;
        int y1 = getY() + 1 + (int)(scrollPercentage * (height - scrollerSize - 2));
        int w = width - 2;
        int h = scrollerSize;

        if (getStyle().isCustom()) {
            DynamicGuiRenderer.renderArea(graphics, x1, y1, w, h, getFontColor(), style, isActive() ? (isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON) : ButtonState.DISABLED);
        } else {
            GuiUtils.setTint(getFontColor());
            GuiUtils.drawTexture(DragonLib.UI, graphics, x1, y1, 2, 2, startU, startV, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top left
            GuiUtils.drawTexture(DragonLib.UI, graphics, x1, y1 + h - 2, 2, 2, startU, startV + 3, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom left
            GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + w - 2, y1, 2, 2, startU + 3, startV, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top right
            GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + w - 2, y1 + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom right

            GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + 2, y1, w - 4, 2, startU + 2, startV, 1, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top
            GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + 2, y1 + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom
            GuiUtils.drawTexture(DragonLib.UI, graphics, x1, y1 + 2, 2, h - 4, startU, startV + 2, 2, 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // left
            GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + w - 2, y1 + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // right
            
            for (int i = 0; i < h - 4; i += 2) {
                GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + 2, y1 + 2 + i, w - 4, i < h - 4 ? 2 : 1, startU + 2, startV + 2, 1, i < h - 4 ? 2 : 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT);
            }
            GuiUtils.resetTint();
        }
    }

    @Override
    protected double getMouseScrollDirection(double pMouseX, double pMouseY) {
        return pMouseY;
    }

    @Override
    protected int getScrollbarLength() {
        return height;
    }

    @Override
    protected int getXorY() {
        return getY();
    }

    @Override
    protected double selectScrollDelta(double deltaX, double deltaY) {
        return deltaY;
    }
}
