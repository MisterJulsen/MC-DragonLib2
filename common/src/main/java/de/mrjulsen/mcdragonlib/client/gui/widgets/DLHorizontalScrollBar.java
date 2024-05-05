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

public class DLHorizontalScrollBar extends DLButton implements IExtendedAreaWidget { 

    public static final int DEFAULT_STEP_SIZE = 1;

    private final GuiAreaDefinition scrollArea;

    private double scrollPercentage;
    private int scroll;
    private int maxScroll = 2;
    private boolean isScrolling = false;

    private int maxColumnsOnPage = 1;
    private int scrollerWidth = 15;
    private int stepSize = DEFAULT_STEP_SIZE;

    private boolean autoScrollerWidth = false;

    // Events
    public Consumer<DLHorizontalScrollBar> onValueChanged;

    public DLHorizontalScrollBar(int x, int y, int w, int h, GuiAreaDefinition scrollArea) {
        super(x, y, Math.max(7, w), Math.max(7, h), null);
        this.scrollArea = scrollArea;
    }

    public DLHorizontalScrollBar(int x, int y, int w, GuiAreaDefinition scrollArea) {
        this(x, y, w, 14, scrollArea);
    }

    public DLHorizontalScrollBar(int x, int y, int w, int h) {
        this(x, y, w, h, null);
    }

    public DLHorizontalScrollBar(int x, int y, int w) {
        this(x, y, w, null);
    }

    public DLHorizontalScrollBar setStepSize(int c) {
        stepSize = Math.max(DEFAULT_STEP_SIZE, c);
        return this;
    }

    public DLHorizontalScrollBar setMaxColumnsOnPage(int c) {
        maxColumnsOnPage = Math.max(1, c);
        return this;
    }

    public DLHorizontalScrollBar setAutoScrollerWidth(boolean b) {
        autoScrollerWidth = b;
        return this;
    }

    public DLHorizontalScrollBar setScrollerWidth(int w) {
        scrollerWidth = Math.max(5, w);
        return this;
    }

    public DLHorizontalScrollBar updateMaxScroll(int columns) {
        this.maxScroll = Math.max(columns - maxColumnsOnPage, 0);
        if (autoScrollerWidth) {
            this.scrollerWidth = Math.max((int)((width - 2) / Math.max(columns / (float)maxColumnsOnPage, 1.0f)), 5);
        }
        return this;
    }

    public DLHorizontalScrollBar withOnValueChanged(Consumer<DLHorizontalScrollBar> event) {
        this.onValueChanged = event;
        return this;
    }

    public boolean getAutoScrollerWidth() {
        return this.autoScrollerWidth;
    }

    public int getScrollValue() {
        return scroll;
    }

    public int getMaxScroll() {
        return maxScroll;
    }

    public int getMaxColumnsOnPage() {
        return maxColumnsOnPage;
    }



    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (isMouseOver(pMouseX, pMouseY) && canScroll()) {
            isScrolling = true;
            scrollTo(pMouseX);
        }
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        if (this.isScrolling) {
            scrollTo(pMouseX);
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

    private void scrollTo(double mousePos) {
        int i = getX() + 1;
        int j = i + width - 2;

        this.scrollPercentage = (mousePos - (double)i - ((double)scrollerWidth / 2.0D)) / (double)(j - i - scrollerWidth);
        this.scrollPercentage = MathUtils.clamp(this.scrollPercentage, 0.0F, 1.0F);
        scroll = Math.max(0, (int)Math.round(scrollPercentage * maxScroll));
        
        if (onValueChanged != null)
            onValueChanged.accept(this);
    }

    public void scrollToColumn(int pos) {
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

        int y1 = getY() + 1;
        int x1 = getX() + 1 + (int)(scrollPercentage * (width - scrollerWidth - 2));
        int w = scrollerWidth;
        int h = height - 2;

        GuiUtils.drawTexture(DragonLib.UI, graphics, x1, y1, 2, 2, startU, startV, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x1, y1 + h - 2, 2, 2, startU, startV + 3, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + w - 2, y1, 2, 2, startU + 3, startV, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top right
        GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + w - 2, y1 + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom right

        GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + 2, y1, w - 4, 2, startU + 2, startV, 1, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top
        GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + 2, y1 + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom
        GuiUtils.drawTexture(DragonLib.UI, graphics, x1, y1 + 2, 2, h - 4, startU, startV + 2, 2, 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + w - 2, y1 + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // right
        
        for (int i = 0; i < w - 4; i += 2) {
            GuiUtils.drawTexture(DragonLib.UI, graphics, x1 + 2 + i, y1 + 2, i < w - 4 ? 2 : 1, h - 4, startU + 2, startV + 2, i < w - 4 ? 2 : 1, 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT);
        }
    }

    @Override
    public boolean isInArea(double mouseX, double mouseY) {
        return scrollArea == null || isMouseOver(mouseX, mouseY) || scrollArea.isInBounds(mouseX, mouseY);
    }  
}
