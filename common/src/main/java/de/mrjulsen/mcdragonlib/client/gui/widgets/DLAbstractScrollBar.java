package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;

public abstract class DLAbstractScrollBar<T extends DLAbstractScrollBar<T>> extends DLButton implements IExtendedAreaWidget { 

    public static final int DEFAULT_STEP_SIZE = 1;
    public static final int DEFAULT_SCROLLBAR_THICKNESS = 14;
    public static final int MIN_SCROLLBAR_THICKNESS = 7;
    public static final int MIN_SCROLLER_SIZE = 5;

    protected GuiAreaDefinition scrollArea;

    protected double scrollPercentage;
    protected double scroll;
    protected int maxScroll = 2;
    protected boolean isScrolling = false;

    protected int maxUnitsPerPage = 1;
    protected int scrollerSize = 15;
    protected int stepSize = DEFAULT_STEP_SIZE;

    protected boolean autoScrollerSize = false;

    // Events
    public Consumer<T> onValueChanged;

    protected DLContextMenu menu = new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
        DLContextMenuItem.Builder builder = new DLContextMenuItem.Builder();
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.scrollbar.here"), Sprite.empty(), true, (b) -> {
            scrollToMouse(getMouseScrollDirection(b.x(), b.y()));
        }, null));
        builder.addSeparator();
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.scrollbar.top"), Sprite.empty(), getScrollValue() > 0, (b) -> {
            scrollToTop();
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.scrollbar.bottom"), Sprite.empty(), getScrollValue() < getMaxScroll(), (b) -> {
            scrollToBottom();
        }, null));
        builder.addSeparator();
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.scrollbar.page_up"), Sprite.empty(), getScrollValue() > 0, (b) -> {
            setScrollValue(getScrollValue() - getScreenSize());
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.scrollbar.page_down"), Sprite.empty(), getScrollValue() < getMaxScroll(), (b) -> {
            setScrollValue(getScrollValue() + getScreenSize());
        }, null));
        builder.addSeparator();
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.scrollbar.step_up"), Sprite.empty(), getScrollValue() > 0, (b) -> {
            setScrollValue(getScrollValue() - stepSize);
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.scrollbar.step_down"), Sprite.empty(), getScrollValue() < getMaxScroll(), (b) -> {
            setScrollValue(getScrollValue() + stepSize);
        }, null));
        return builder;
    });

    public DLAbstractScrollBar(int x, int y, int w, int h, GuiAreaDefinition scrollArea) {
        super(x, y, Math.max(MIN_SCROLLBAR_THICKNESS, w), Math.max(MIN_SCROLLBAR_THICKNESS, h), null);
        this.scrollArea = scrollArea;
        setRenderStyle(AreaStyle.GRAY);
        setMenu(menu);
    }

    public DLAbstractScrollBar(int x, int y, int w, int h) {
        this(x, y, w, h, null);
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T)this;
    }


    public T setStepSize(int c) {
        stepSize = Math.max(DEFAULT_STEP_SIZE, c);
        return self();
    }

    public T setScreenSize(int c) {
        maxUnitsPerPage = Math.max(1, c);
        return self();
    }

    public T setAutoScrollerSize(boolean b) {
        autoScrollerSize = b;
        return self();
    }

    public T setScrollerSize(int w) {
        scrollerSize = Math.max(MIN_SCROLLER_SIZE, w);
        return self();
    }
    
    public T setMaxScroll(int max) {
        this.maxScroll = Math.max(max - maxUnitsPerPage, 0);
        if (autoScrollerSize) {
            this.scrollerSize = Math.max((int)((getScrollbarLength() - 2) / Math.max(max / (float)maxUnitsPerPage, 1.0f)), 5);
        }
        this.setScrollValue(getScrollValue());
        return self();
    }

    @Deprecated(forRemoval = true)
    public T updateMaxScroll(int max) {
        return setMaxScroll(max);
    }

    public T withOnValueChanged(Consumer<T> event) {
        this.onValueChanged = event;
        return self();
    }

    public T setScrollArea(GuiAreaDefinition area) {
        this.scrollArea = area;
        return self();
    }

    public boolean getAutoScrollerSize() {
        return this.autoScrollerSize;
    }

    public double getScrollValue() {
        return scroll;
    }

    public int getMaxScroll() {
        return maxScroll;
    }

    public int getScreenSize() {
        return maxUnitsPerPage;
    }

    public GuiAreaDefinition getScrollArea() {
        return scrollArea;
    }



    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (isMouseOver(pMouseX, pMouseY) && canScroll()) {
            isScrolling = true;
            scrollToMouse(getMouseScrollDirection(pMouseX, pMouseY));
        }
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        if (this.isScrolling) {
            scrollToMouse(getMouseScrollDirection(pMouseX, pMouseY));
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (canScroll()) {
            scroll = MathUtils.clamp((scroll - pDelta * stepSize), 0, maxScroll);

            int i = maxScroll;
            this.scrollPercentage = (double)this.scrollPercentage - pDelta * stepSize / (double)i;
            this.scrollPercentage = MathUtils.clamp(this.scrollPercentage, 0.0F, 1.0F);
            
            if (onValueChanged != null)
                onValueChanged.accept(self());

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
        int i = getXorY() + 1;
        int j = i + getScrollbarLength() - 2;

        this.scrollPercentage = (mousePos - (double)i - ((double)scrollerSize / 2.0D)) / (double)(j - i - scrollerSize);
        this.scrollPercentage = MathUtils.clamp(this.scrollPercentage, 0.0F, 1.0F);
        scroll = Math.max(0, Math.round(scrollPercentage * maxScroll));
        
        if (onValueChanged != null)
            onValueChanged.accept(self());
    }

    public void scrollTo(int pos) {
        scroll = MathUtils.clamp(pos, 0, getMaxScroll());
        
        if (onValueChanged != null)
            onValueChanged.accept(self());
    }

    public boolean canScroll() {
        return maxScroll > 0;
    }

    public void setScrollValue(double scroll) {
        this.scroll = MathUtils.clamp(scroll, 0, this.maxScroll);
        this.scrollPercentage = 1D / this.maxScroll * this.scroll;
        DLUtils.doIfNotNull(onValueChanged, x -> x.accept(self()));
    }

    public void scrollToTop() {
        setScrollValue(0);
    }

    public void scrollToBottom() {
        setScrollValue(maxScroll);
    }



    /**
     * @return The correct mouse parameter for the direction of the scrollbar.
     */
    protected abstract double getMouseScrollDirection(double pMouseX, double pMouseY);
    protected abstract int getScrollbarLength();
    protected abstract int getXorY();

    @Override
    public abstract void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick);

    @Override
    public boolean isInArea(double mouseX, double mouseY) {
        return scrollArea == null || isMouseOver(mouseX, mouseY) || scrollArea.isInBounds(mouseX, mouseY);
    }  
}
