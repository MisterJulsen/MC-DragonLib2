package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.Alignment;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.gui.v2.SizeMode;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.input.ResizeHandle;
import de.mrjulsen.mcdragonlib.gui.v2.input.WheelScroll;
import de.mrjulsen.mcdragonlib.gui.v2.layout.StackLayout;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.theme.BoxStyle;
import de.mrjulsen.mcdragonlib.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLListUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class DLListBox<T> extends GuiComponent {

    public record SelectedItemsChanged(List<?> items, List<Integer> indices) implements IEvent {}
    public record ItemActivation(Optional<?> item, int index) implements IEvent {}
    public record ItemCheckedChanged(Optional<?> item, int index, boolean checked) implements IEvent {}

    public enum CheckMode {
        NONE,
        MULTIPLE,
        SINGLE
    }

    public static class ItemPanel<T> extends GuiComponent {

        public record RowLayout(double checkX, double iconX, double textX) {}

        protected final WheelScroll scroll;
        protected final DLListBox<T> container;

        private int hoveredIndex = INVALID;

        public ItemPanel(DLListBox<T> container) {
            super(0, 0, 0, 0);
            this.container = container;

            addEventListener(GuiEvents.MouseMoveEvent.class, (s, e) -> {
                Rectangle bounds = itemBounds();
                double mouseX = e.localX(s);
                double mouseY = e.localY(s);
                if (bounds.collision(mouseX, mouseY)) {
                    hoveredIndex = indexAt(mouseY + scrollY() - bounds.top());
                } else {
                    hoveredIndex = INVALID;
                }
            });
            addEventListener(GuiEvents.MouseLeaveEvent.class, (s, e) -> {
                hoveredIndex = INVALID;
            });
            addEventListener(GuiEvents.ScrollChangedEvent.class, (s, e) -> {
                container.scrollbar.value.set(e.newScrollY());
            });
            addEventListener(GuiEvents.ClickEvent.class, (s, e) -> {
                Rectangle bounds = itemBounds();
                double mouseX = e.localX(s);
                double mouseY = e.localY(s);

                if (bounds.collision(mouseX, mouseY)) {
                    int idx = indexAt(mouseY + scrollY() - bounds.top());
                    container.itemClick(idx, mouseX - bounds.left());
                }
            });

            scroll = new WheelScroll();
            addBehaviour(scroll);

            addEventListener(GuiEvents.LayoutUpdatedEvent.class, (s, e) -> {
                container.scrollbar.max.set((int)itemsHeight());
                container.scrollbar.screenSize.set((int)itemBounds().height());
                container.scrollbar.stepSize.set(container.itemHeight.get());
                scroll.step.set((double)container.itemHeight.get());
            });
        }

        @Override
        protected Size measureContent(double availableWidth, double availableHeight) {
            return Size.of(0, itemsHeight() + getThemeValue(THEME_KEY_ITEM_MARGIN).vertical());
        }

        protected double itemsHeight() {
            return (double)container.items.size() * container.itemHeight.get();
        }

        protected Rectangle itemBounds() {
            Insets margin = getThemeValue(THEME_KEY_ITEM_MARGIN);
            return Rectangle.withPoints(margin.left(), margin.top(), dWidth() - margin.right(), dHeight() - margin.bottom());
        }

        protected RowLayout rowLayout() {
            double iconX = container.isCheckable() ? container.itemHeight.get() : 1;
            return new RowLayout(0, iconX, iconX + (container.showIcons.get() ? container.iconSize.get() + 2 : 2));
        }

        protected int indexAt(double localY) {
            return (int)(localY / container.itemHeight.get());
        }

        @Override
        protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
            container.getThemeValue(THEME_KEY_ITEM_RENDERER).render(g, container, 0, 0, dWidth(), dHeight());
        }
    }

    public static class Renderer implements WidgetRenderer<DLListBox<?>> {

        @Override
        public void render(DLGuiGraphics g, DLListBox<?> component, double x, double y, double width, double height) {
            Rectangle itemBounds = component.contentPanel.itemBounds();
            g.pushClip(component.contentPanel, itemBounds.left(), itemBounds.top(), itemBounds.width(), itemBounds.height());
            int startIdx = component.contentPanel.indexAt(component.contentPanel.scrollY());
            int endIdx = component.contentPanel.indexAt(component.contentPanel.scrollY() + itemBounds.height()) + 1;
            ItemPanel.RowLayout layout = component.contentPanel.rowLayout();
            for (int i = Math.max(0, startIdx); i < Math.min(component.items.size(), endIdx); i++) {
                double dY = i * component.itemHeight.get() - component.contentPanel.scrollY() + itemBounds.top();
                int h = component.itemHeight.get();
                g.pushLocalSpace(itemBounds.left(), dY);
                renderItem(g, component, i, layout, itemBounds.width(), h);
                g.popLocalSpace();
            }
            g.popClip();
        }

        protected void renderListBox(DLGuiGraphics g, DLListBox<?> component, double width, double height) {
            GuiUtils.box(g, 0, 0, width, height, component.getThemeValue(THEME_KEY_COLOR).fill(), component.getThemeValue(THEME_KEY_COLOR).border());
        }

        protected void renderItem(DLGuiGraphics g, DLListBox<?> component, int itemIndex, ItemPanel.RowLayout layout, double width, double height) {
            boolean selected = component.isSelected(itemIndex);
            boolean hovered = component.contentPanel.hoveredIndex == itemIndex;
            renderItemBackground(g, component, itemIndex, selected, hovered, width, height);
            if (component.isCheckable()) {
                renderCheckbox(g, component, itemIndex, hovered, layout.checkX(), layout.iconX() - layout.checkX(), height);
            }
            if (component.showIcons.get()) {
                renderIcon(g, component, itemIndex, layout.iconX(), height);
            }
            renderText(g, component, itemIndex, selected, layout.textX(), height);
        }

        protected void renderItemBackground(DLGuiGraphics g, DLListBox<?> component, int itemIndex, boolean selected, boolean hovered, double width, double height) {
            if (selected) {
                GuiUtils.box(g, 0, 0, width, height, component.getThemeValue(THEME_KEY_ITEM_SELECTION_COLOR).fill(), component.getThemeValue(THEME_KEY_ITEM_SELECTION_COLOR).border());
            } else if (hovered) {
                GuiUtils.box(g, 0, 0, width, height, component.getThemeValue(THEME_KEY_ITEM_HOVER_COLOR).fill(), component.getThemeValue(THEME_KEY_ITEM_HOVER_COLOR).border());
            }
        }

        protected void renderCheckbox(DLGuiGraphics g, DLListBox<?> component, int itemIndex, boolean hovered, double x, double width, double height) {
            String name = (component.checkMode.get() == CheckMode.SINGLE ? "radiobutton" : "checkbox") + (component.isChecked(itemIndex) ? "_checked" : "") + (hovered ? "_selected" : "");
            if (DLTextureSheet.CHECKBOX.getSprite(name) instanceof DLTextureSheetData.StretchedSprite s) {
                double w = Math.min(s.width(), width - 2);
                double h = Math.min(s.height(), height - 2);
                GuiUtils.sprite(g, s, x + (width - w) / 2, (height - h) / 2, w, h);
            }
        }

        protected void renderIcon(DLGuiGraphics g, DLListBox<?> component, int itemIndex, double x, double height) {
            DLSprite icon = component.formatIcon(itemIndex);
            if (icon == null || icon.isEmpty()) {
                return;
            }
            int size = component.iconSize.get();
            float scale = (float)size / Math.max(1, Math.max(icon.getWidth(), icon.getHeight()));
            g.pose().pushPose();
            g.pose().translate(x, (height - size) / 2, 0);
            g.pose().scale(scale, scale, 1);
            icon.render(g.legacy(), 0, 0);
            g.pose().popPose();
        }

        protected void renderText(DLGuiGraphics g, DLListBox<?> component, int itemIndex, boolean selected, double x, double height) {
            GuiUtils.text(g, component.formatItem(itemIndex), x, TextUtils.centerTextY(g.font(), height), component.getThemeValue(selected ? Theme.TEXT_HIGHLIGHT : Theme.TEXT), false);
        }
    }

    public static final int INVALID = -1;

    public static final Theme.Key<Renderer> THEME_KEY_ITEM_RENDERER = Theme.key(DragonLib.MODID + ".listbox.item_renderer", new Renderer());
    public static final Theme.Key<Insets> THEME_KEY_ITEM_MARGIN = Theme.key(DragonLib.MODID + ".listbox.item_margin", Insets.of(0));
    public static final Theme.Key<BoxStyle> THEME_KEY_COLOR = Theme.key(DragonLib.MODID + ".listbox.color", new BoxStyle(Theme.BORDER_COLOR, DLColor.fromInt(0x88000000)));
    public static final Theme.Key<BoxStyle> THEME_KEY_ITEM_SELECTION_COLOR = Theme.key(DragonLib.MODID + ".listbox.selection_color", new BoxStyle(DLColor.WHITE, Theme.BACKGROUND_COLOR));
    public static final Theme.Key<BoxStyle> THEME_KEY_ITEM_HOVER_COLOR = Theme.key(DragonLib.MODID + ".listbox.hover_color", new BoxStyle(Theme.BORDER_COLOR, Theme.BACKGROUND_COLOR));

    private final IntOpenHashSet selectedLookup = new IntOpenHashSet();
    private final Set<T> checkedItems = new HashSet<>();

    public final ListProperty<T> items = new ListProperty<T>().withUpdateCallback((val, operation) -> onItemsChanged(operation));
    public final ListProperty<T> selectedItems = new ListProperty<>(List.of());
    public final ListProperty<Integer> selectedIndices = new ListProperty<Integer>(List.of()).withUpdateCallback((val, operation) -> {
        selectedLookup.clear();
        selectedLookup.addAll(val);
    });
    public final Property<Function<T, Component>> textFormat = new Property<>((item) -> TextUtils.text(String.valueOf(item)));
    public final Property<Function<T, DLSprite>> icons = new Property<>((item) -> null);
    public final Property<Predicate<T>> checkedGetter = new Property<>(checkedItems::contains);
    public final Property<BiConsumer<T, Boolean>> checkedSetter = new Property<>((item, checked) -> {
        if (checked) {
            checkedItems.add(item);
        } else {
            checkedItems.remove(item);
        }
    });
    public final BooleanProperty drawFontShadow = new BooleanProperty(true);
    public final BooleanProperty multiselect = new BooleanProperty(false);
    public final BooleanProperty allowNoSelection = new BooleanProperty(true);
    public final BooleanProperty showIcons = new BooleanProperty(false);
    public final BooleanProperty allowUncheck = new BooleanProperty(true);
    public final BooleanProperty checkOnClick = new BooleanProperty(false);
    public final Property<CheckMode> checkMode = new Property<>(CheckMode.NONE);
    public final NumberProperty<Integer> itemHeight = new NumberProperty<>(14, 1, Integer.MAX_VALUE);
    public final NumberProperty<Integer> iconSize = new NumberProperty<>(12, 1, Integer.MAX_VALUE);
    public final NumberProperty<Integer> maxVisibleItems = new NumberProperty<>(Integer.MAX_VALUE, 1, Integer.MAX_VALUE);

    private final DLScrollBar scrollbar;
    private final ItemPanel<T> contentPanel;

    private int lastClickedIndex = 0;
    private int checkedIndex = INVALID;
    private boolean syncingSelection = false;
    private double cachedTextWidth = INVALID;

    public DLListBox(int x, int y, int width, int height) {
        super(x, y, width, height);

        StackLayout layout = new StackLayout(StackLayout.Direction.HORIZONTAL);
        layout.crossAlignment.set(Alignment.STRETCH);
        this.layout.set(layout);

        this.padding.set(Insets.of(1));
        this.focusable.set(true);

        contentPanel = new ItemPanel<>(this);
        contentPanel.widthMode.set(SizeMode.FILL);
        addComponent(contentPanel);

        scrollbar = new DLScrollBar(0, 0, 7, 20, DLScrollBar.Orientation.VERTICAL);
        scrollbar.overrideThemeKey(DLScrollBar.THEME_KEY_RENDERER, DLScrollBar.Renderer.SIMPLE);
        addComponent(scrollbar);


        this.textFormat.addChangeListener((o, n) -> {
            cachedTextWidth = INVALID;
            invalidateLayout();
            invokeEvent(this, new DLItemPicker.TextFormatChangedEvent());
        });

        this.selectedIndices.addChangeListener((o, n) -> {
            if (syncingSelection || o.equals(n)) {
                return;
            }
            List<T> itms = new ArrayList<>(selectedIndices.size());
            for (int i : selectedIndices.get()) {
                T item = items.get(i);
                if (item != null) {
                    itms.add(item);
                }
            }
            syncingSelection = true;
            selectedItems.set(itms);
            syncingSelection = false;
            invokeEvent(this, new SelectedItemsChanged(itms, n));
        });
        this.selectedItems.addChangeListener((o, n) -> {
            if (!syncingSelection && !DLListUtils.compareCollections(o, n, Objects::equals)) {
                selectedIndices.set(DLListUtils.indicesOf(items.get(), selectedItems.get()));
            }
        });

        scrollbar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            contentPanel.setScrollY(e.value());
        });
        addEventListener(GuiEvents.KeyPressEvent.class, (s, e) -> {
            if (e.keyCode() == GLFW.GLFW_KEY_SPACE && isCheckable() && !selectedIndices.isEmpty()) {
                boolean checked = !isChecked(selectedIndices.get(0));
                for (int i : checkMode.get() == CheckMode.SINGLE ? selectedIndices.get().subList(0, 1) : selectedIndices.get()) {
                    setChecked(i, checked);
                }
                e.consume();
            }
        });
        this.showIcons.addChangeListener((o, n) -> invalidateLayout());
        this.checkMode.addChangeListener((o, n) -> {
            checkedIndex = INVALID;
            updateSelectable();
            invalidateLayout();
        });
        this.checkOnClick.addChangeListener((o, n) -> updateSelectable());
        this.iconSize.addChangeListener((o, n) -> invalidateLayout());
        this.itemHeight.addChangeListener((o, n) -> contentPanel.invalidateLayout());
        this.maxVisibleItems.addChangeListener((o, n) -> invalidateLayout());
    }

    @Override
    protected Size measureContent(double availableWidth, double availableHeight) {
        Insets margin = contentPanel.getThemeValue(THEME_KEY_ITEM_MARGIN);
        double rows = Math.min(items.size(), maxVisibleItems.get());
        double w = widthMode.get() == SizeMode.AUTO ? contentPanel.rowLayout().textX() + textWidth() + 3 + margin.horizontal() + scrollbar.dWidth() : 0;
        return Size.of(w, rows * itemHeight.get() + margin.vertical());
    }

    public double heightForItems(int count) {
        return (double)count * itemHeight.get() + contentPanel.getThemeValue(THEME_KEY_ITEM_MARGIN).vertical() + padding.get().vertical();
    }

    public int itemsFitting(double height) {
        return Math.max(0, (int)((height - heightForItems(0)) / itemHeight.get()));
    }

    public void scrollToItem(T item) {
        scrollToItem(items.indexOf(item));
    }

    public void scrollToItem(int index) {
        if (index < 0 || index >= items.size()) return;

        double h = itemHeight.get();
        double top = index * h;
        double bottom = top + h;
        double view = contentPanel.itemBounds().height();
        double scroll = contentPanel.scrollY();

        if (bottom > scroll + view) {
            contentPanel.setScrollY(bottom - view);
        } else if (top < scroll) {
            contentPanel.setScrollY(top);
        }
    }

    protected double textWidth() {
        if (cachedTextWidth < 0) {
            int max = 0;
            for (T item : items) {
                max = Math.max(max, TextUtils.textWidth(TextUtils.font(), textFormat.get().apply(item)));
            }
            cachedTextWidth = max;
        }
        return cachedTextWidth;
    }

    public boolean isSelected(int index) {
        return selectedLookup.contains(index);
    }

    public boolean isChecked(int index) {
        return checkedGetter.get().test(items.get(index));
    }

    public boolean isCheckable() {
        return checkMode.get() != CheckMode.NONE;
    }

    public boolean isSelectable() {
        return !isCheckable() || !checkOnClick.get();
    }

    protected void updateSelectable() {
        if (!isSelectable() && !selectedIndices.isEmpty()) {
            selectedIndices.clear();
        }
    }

    public void setChecked(int index, boolean checked) {
        if (index < 0 || index >= items.size() || isChecked(index) == checked) {
            return;
        }
        if (checkMode.get() == CheckMode.SINGLE) {
            if (!checked && !allowUncheck.get()) {
                return;
            }
            int previous = getCheckedIndex();
            if (checked && previous != INVALID) {
                applyChecked(previous, false);
            }
            checkedIndex = checked ? index : INVALID;
        }
        applyChecked(index, checked);
    }

    public int getCheckedIndex() {
        if (checkedIndex < 0 || checkedIndex >= items.size() || !isChecked(checkedIndex)) {
            checkedIndex = INVALID;
            for (int i = 0; i < items.size() && checkedIndex == INVALID; i++) {
                if (isChecked(i)) {
                    checkedIndex = i;
                }
            }
        }
        return checkedIndex;
    }

    private void applyChecked(int index, boolean checked) {
        T item = items.get(index);
        checkedSetter.get().accept(item, checked);
        invokeEvent(this, new ItemCheckedChanged(Optional.ofNullable(item), index, checked));
    }

    public List<T> getCheckedItems() {
        return items.stream().filter(checkedGetter.get()).toList();
    }

    protected void onItemsChanged(ListProperty.ListOperation operation) {
        cachedTextWidth = INVALID;
        contentPanel.invalidateLayout();
        if (operation != ListProperty.ListOperation.ADD && !checkedItems.isEmpty()) {
            checkedItems.retainAll(new HashSet<>(items.get()));
        }
        updateSelectedItem(operation == ListProperty.ListOperation.ADD || operation == ListProperty.ListOperation.REPLACE);
    }

    protected void updateSelectedItem(boolean firstIfUnselected) {
        if (!selectedIndices.isEmpty()) {
            selectedIndices.set(DLListUtils.indicesOf(items.get(), selectedItems.get()));
        } else if (firstIfUnselected && !allowNoSelection.get() && isSelectable() && !items.isEmpty()) {
            selectedIndices.set(List.of(0));
        }
    }

    protected final Component formatItem(int index) {
        return textFormat.get().apply(items.get(index));
    }

    protected final DLSprite formatIcon(int index) {
        return icons.get().apply(items.get(index));
    }


    protected void itemClick(int index, double localX) {
        if (index < 0 || index >= items.size()) {
            return;
        }

        if (isCheckable() && (checkOnClick.get() || localX < contentPanel.rowLayout().iconX())) {
            setChecked(index, !isChecked(index));
            return;
        }
        if (multiselect.get() && Screen.hasShiftDown() && lastClickedIndex >= 0) {
            boolean keep = Screen.hasControlDown();
            List<Integer> selection = keep ? new ArrayList<>(selectedIndices.get()) : new ArrayList<>();
            for (int i = Math.min(lastClickedIndex, index); i <= Math.max(lastClickedIndex, index); i++) {
                if (!keep || !isSelected(i)) {
                    selection.add(i);
                }
            }
            selectedIndices.set(selection);
            return;
        }

        if (Screen.hasControlDown() && isSelected(index)) {
            if (allowNoSelection.get() || selectedIndices.size() > 1) {
                selectedIndices.remove((Object)index);
            }
        } else if (Screen.hasControlDown() && multiselect.get()) {
            selectedIndices.add(index);
        } else {
            selectedIndices.set(List.of(index));
        }

        lastClickedIndex = index;

        invokeEvent(this, new ItemActivation(Optional.ofNullable(items.get(index)), index));
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        getThemeValue(THEME_KEY_ITEM_RENDERER).renderListBox(g, this, dWidth(), dHeight());
    }
}
