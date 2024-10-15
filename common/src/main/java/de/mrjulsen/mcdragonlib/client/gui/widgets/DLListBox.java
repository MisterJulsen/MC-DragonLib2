package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class DLListBox<T> extends WidgetContainer implements Collection<DLListBox.DLListBoxItem<T>> {

    private int itemHeight = 16;
    private boolean multiselect;
    private Consumer<DLListBox<T>> onSelectionChanged;
    private Consumer<DLListBox<T>> onItemsChanged;
    private final DLVerticalScrollBar scrollBar;
    private final DLListBoxContainer<T> container;

    public DLListBox(int x, int y, int width, int height, boolean multiselect) {
        super(x, y, width, height);
        this.multiselect = multiselect;
        this.scrollBar = addRenderableWidget(new DLVerticalScrollBar(getX() + getWidth() - 9, getY() + 1, 8, getHeight() - 2, GuiAreaDefinition.of(this)));
        this.container = addRenderableWidget(new DLListBoxContainer<>(getX() + 1, getY() + 1, getWidth() - 10, getHeight() - 2));
        scrollBar.setScreenSize(getHeight());
        scrollBar.updateMaxScroll(0);
        scrollBar.setStepSize(8);
        scrollBar.setAutoScrollerSize(true);
        scrollBar.withOnValueChanged((bar) -> {
            container.setYScrollOffset(bar.getScrollValue());
        });
    }

    public DLListBox<T> withItemHeight(int height) {
        this.itemHeight = height;
        return this;
    }

    public DLListBox<T> withOnSelectionChangedEvent(Consumer<DLListBox<T>> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
        return this;
    }

    public DLListBox<T> withOnItemsChangedEvent(Consumer<DLListBox<T>> onItemsChanged) {
        this.onItemsChanged = onItemsChanged;
        return this;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public boolean allowsMultiselect() {
        return multiselect;
    }

    public void setMultiselectable(boolean b) {
        this.multiselect = b;
    }

    public DLVerticalScrollBar getScrollBar() {
        return scrollBar;
    }

    @SuppressWarnings("unchecked")
    public List<DLListBoxItem<T>> getSelectedItems() {
        return container.children().stream().filter(x -> x instanceof DLListBoxItem item && item.isSelected()).map(x -> (DLListBoxItem<T>)x).toList();
    }

    public int getSelectedCount() {
        return getSelectedItems().size();
    }
    
    @SuppressWarnings("unchecked")
    public Collection<DLListBoxItem<T>> getItems() {
        return container.children().stream().filter(x -> x instanceof DLListBoxItem).map(x -> (DLListBoxItem<T>)x).toList();
    }
    
    @Override
    public int size() {
        return getItems().size();
    }

    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public boolean contains(Object o) {
        return getItems().contains(o);
    }

    @Override
    public Iterator<DLListBoxItem<T>> iterator() {
        return getItems().iterator();
    }

    @Override
    public Object[] toArray() {
        return getItems().toArray();
    }

    @Override
    public <A> A[] toArray(A[] a) {
        return getItems().toArray(a);
    }

    public DLListBoxItem<T> add(DLListBoxItemBuilder<T> builder) {        
        DLListBoxItem<T> item = container.addRenderableWidget(builder.build(this));
        DLUtils.doIfNotNull(onItemsChanged, x -> x.accept(this));
        scrollBar.updateMaxScroll(itemHeight * size() + 2);
        return item;
    }

    /**
     * @deprecated Use the method with the Builder instead!
     */
    @Override
    @Deprecated(forRemoval = true)
    public boolean add(DLListBoxItem<T> e) {
        throw new IllegalAccessError("You cannot add a DLListBoxItems directly. Use the DLListBoxItemBuilder instead.");
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof DLListBoxItem l) {
            container.removeWidget(l);
            DLUtils.doIfNotNull(onItemsChanged, x -> x.accept(this));
            scrollBar.updateMaxScroll(itemHeight * size() + 2);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(x -> contains(x));
    }

    /**
     * @deprecated Use the method with the Builder instead!
     */
    @Override
    @Deprecated(forRemoval = true)
    public boolean addAll(Collection<? extends DLListBoxItem<T>> c) {
        throw new IllegalAccessError("You cannot add a DLListBoxItems directly. Use the DLListBoxItemBuilder instead.");
    }
    
    public boolean addAllItems(Collection<DLListBoxItemBuilder<T>> c) {
        c.forEach(x -> add(x));
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return c.stream().allMatch(x -> remove(x));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        getItems().stream().filter(x -> !c.contains(x)).forEach(x -> remove(x));
        return true;
    }

    @Override
    public void clear() {
        getItems().forEach(x -> remove(x));
        DLUtils.doIfNotNull(onItemsChanged, x -> x.accept(this));
        scrollBar.updateMaxScroll(itemHeight * size() + 2);
    }

    public void checkSelection(DLListBoxItem<T> selected) {
        if (!multiselect) {
            getItems().stream().filter(x -> x != selected).forEach(x -> x.setSelected(false));
        }
        DLUtils.doIfNotNull(onSelectionChanged, x -> x.accept(this));
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        DynamicGuiRenderer.renderArea(graphics, getX(), getY(), getWidth(), getHeight(), 0xFF666666, AreaStyle.GRAY, ButtonState.DOWN);
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return true;
    }

    private static class DLListBoxContainer<T> extends ScrollableWidgetContainer {

        public DLListBoxContainer(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput var1) { }

        @Override
        public boolean consumeScrolling(double mouseX, double mouseY) {
            return false;
        }
    }

    public static class DLListBoxItem<T> extends DLButton {

        protected final DLListBox<T> parent;
        protected final T data;

        protected Sprite icon;
        protected boolean useCustomRenderer = true;
        protected boolean selected;

        @SuppressWarnings("unchecked")
        public DLListBoxItem(DLListBox<T> parent, Component pMessage, Sprite icon, T data, Consumer<DLListBoxItem<T>> pOnPress) {
            super(parent.getX() + 1, parent.getY() + 1 + parent.size() * parent.getItemHeight(), parent.getWidth() - 2 - parent.getScrollBar().getWidth(), parent.getItemHeight(), pMessage,
            (btn) -> {
                DLListBoxItem<T> self = (DLListBoxItem<T>)btn;
                self.setSelected(!self.isSelected());
                pOnPress.accept(self);
            });
            this.parent = parent;
            this.data = data;
            this.icon = icon;
        }

        @Override
        public void setRenderStyle(AreaStyle style) {
            super.setRenderStyle(style);
            setUsingCustomRenderer(false);
        }

        public DLListBox<T> getParent() {
            return parent;
        }

        public T getData() {
            return data;
        }

        public Sprite getIcon() {
            return icon;
        }

        public boolean isUsingCustomRenderer() {
            return useCustomRenderer;
        }

        public void setUsingCustomRenderer(boolean b) {
            this.useCustomRenderer = b;
        }

        public void setIcon(Sprite icon) {
            this.icon = icon;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean b) {
            this.selected = b;
        }

        @Override
        public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isUsingCustomRenderer()) {
                if (isSelected()) {                    
                    GuiUtils.drawBox(graphics, GuiAreaDefinition.of(this), 0xFF000000, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
                }
                if (isMouseSelected()) {
                    GuiUtils.fill(graphics, x(), y(), width(), height(), 0x33FFFFFF);
                }
            } else {
                DynamicGuiRenderer.renderArea(graphics, x, y, width, height, getBackColor(), style, isActive() ? (isSelected() ? ButtonState.DOWN : (isFocused() || isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON)) : ButtonState.DISABLED);
            }

            int xCoord = x() + height() / 2 - getIcon().getWidth() / 2;
            getIcon().render(graphics, xCoord, y() + getHeight() / 2 -  getIcon().getHeight() / 2);
            GuiUtils.drawString(graphics, font, xCoord + getIcon().getWidth() + 5, y() + getHeight() / 2 - font.lineHeight / 2, getMessage(), isSelected() || isMouseSelected() ? DragonLib.NATIVE_BUTTON_FONT_COLOR_HIGHLIGHT : DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);
        }
    }

    public static record DLListBoxItemBuilder<T>(Component text, Sprite icon, T data, Consumer<DLListBoxItem<T>> onClick) {
        public DLListBoxItem<T> build(DLListBox<T> parent) {
            return new DLListBoxItem<T>(parent, text, icon, data, (item) -> {
                parent.checkSelection(item);
                onClick.accept(item);
            });
        }
    }
}
