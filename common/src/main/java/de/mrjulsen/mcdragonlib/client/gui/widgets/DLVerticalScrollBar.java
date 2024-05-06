package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.MathUtils;

public class DLVerticalScrollBar extends DLButton implements IExtendedAreaWidget { 

    public static final int DEFAULT_STEP_SIZE = 1;
    public static final int DEFAULT_SCROLLBAR_WIDTH = 14;
    public static final int MIN_SCROLLBAR_WIDTH = 7;
    public static final int MIN_SCROLLER_HEIGHT = 5;

    private final GuiAreaDefinition scrollArea;

    private double scrollPercentage;
    private int scroll;
    private int maxScroll = 2;
    private boolean isScrolling = false;

    private int maxRowsOnPage = 1;
    private int scrollerHeight = 15;
    private int stepSize = DEFAULT_STEP_SIZE;

    private boolean autoScrollerHeight = false;

    // Events
    public Consumer<DLVerticalScrollBar> onValueChanged;

    public DLVerticalScrollBar(int x, int y, int w, int h, GuiAreaDefinition scrollArea) {
        super(x, y, Math.max(MIN_SCROLLBAR_WIDTH, w), Math.max(MIN_SCROLLBAR_WIDTH, h), null);
        this.scrollArea = scrollArea;
    }

    public DLVerticalScrollBar(int x, int y, int h, GuiAreaDefinition scrollArea) {
        this(x, y, DEFAULT_SCROLLBAR_WIDTH, h, scrollArea);
    }

    public DLVerticalScrollBar(int x, int y, int w, int h) {
        this(x, y, w, h, null);
    }

    public DLVerticalScrollBar(int x, int y, int h) {
        this(x, y, h, null);
    }

    public DLVerticalScrollBar setStepSize(int c) {
        stepSize = Math.max(DEFAULT_STEP_SIZE, c);
        return this;
    }

    public DLVerticalScrollBar setPageSize(int c) {
        maxRowsOnPage = Math.max(1, c);
        return this;
    }

    public DLVerticalScrollBar setAutoScrollerHeight(boolean b) {
        autoScrollerHeight = b;
        return this;
    }

    public DLVerticalScrollBar setScrollerHeight(int h) {
        scrollerHeight = Math.max(MIN_SCROLLER_HEIGHT, h);
        return this;
    }

    public DLVerticalScrollBar updateMaxScroll(int rows) {
        this.maxScroll = Math.max(rows - maxRowsOnPage, 0);
        if (autoScrollerHeight) {
            this.scrollerHeight = Math.max((int)((height - 2) / Math.max(rows / (float)maxRowsOnPage, 1.0f)), 5);
        }
        return this;
    }

    public DLVerticalScrollBar setOnValueChangedEvent(Consumer<DLVerticalScrollBar> event) {
        this.onValueChanged = event;
        return this;
    }

    public boolean getAutoScrollerHeight() {
        return this.autoScrollerHeight;
    }

    public int getScrollValue() {
        return scroll;
    }

    public int getMaxScroll() {
        return maxScroll;
    }

    public int getPageSize() {
        return maxRowsOnPage;
    }



    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (isMouseOver(pMouseX, pMouseY) && canScroll()) {
            isScrolling = true;
            scrollToMouse(pMouseY);
        }
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        if (this.isScrolling) {
            scrollToMouse(pMouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (canScroll()) {
            scroll = MathUtils.clamp((int)(scroll - pDelta * stepSize), 0, maxScroll);

            int i = maxScroll;
            this.scrollPercentage = (double)this.scrollPercentage - pDelta * stepSize / (double)i;
            this.scrollPercentage = MathUtils.clamp(this.scrollPercentage, 0.0F, 1.0F);
            
            if (onValueChanged != null)
                onValueChanged.accept(this);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRelease(double pMouseX, double pMouseY) {
        this.isScrolling = false;
    }

    private void scrollToMouse(double mousePos) {
        int i = getY() + 1;
        int j = i + height - 2;

        this.scrollPercentage = (mousePos - (double)i - ((double)scrollerHeight / 2.0D)) / (double)(j - i - scrollerHeight);
        this.scrollPercentage = MathUtils.clamp(this.scrollPercentage, 0.0F, 1.0F);
        scroll = Math.max(0, (int)Math.round(scrollPercentage * maxScroll));
        
        if (onValueChanged != null)
            onValueChanged.accept(this);
    }

    public void scrollTo(int pos) {
        scroll = MathUtils.clamp(pos, 0, getMaxScroll());
        
        if (onValueChanged != null)
            onValueChanged.accept(this);
    }

    public boolean canScroll() {
        return maxScroll > 0;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Render background
        DynamicGuiRenderer.renderArea(graphics, getX(), getY(), width, height, AreaStyle.GRAY, ButtonState.DOWN);

        // Render scrollbar
        int startU = canScroll() ? 20 : 25;
        int startV = 5;

        int x1 = getX() + 1;
        int y1 = getY() + 1 + (int)(scrollPercentage * (height - scrollerHeight - 2));
        int w = width - 2;
        int h = scrollerHeight;

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
    }

    @Override
    public boolean isInArea(double mouseX, double mouseY) {
        return scrollArea == null || isMouseOver(mouseX, mouseY) || scrollArea.isInBounds(mouseX, mouseY);
    }  
}
