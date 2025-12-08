package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.LayoutResult;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaListScrollBarRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import de.mrjulsen.mcdragonlib.util.properties.VirtualProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;

@SupportsEvents({
    DLItemSelectionBox.ItemSelectionChangeEvent.class,
})
public class DLItemSelectionBox<T> extends DLAbstractCollectionComponent<T, DLItemSelectionBox.DLListBoxItem<T>> {
    
    private boolean loopFix = false;

    public record ItemSelectionChangeEvent(DLListBoxItem<?> item, MutableBoolean selected) implements IEvent {}

    public final BooleanProperty multiselect = new BooleanProperty(false);

    public final VirtualProperty<List<T>> selectedItems = new VirtualProperty<List<T>>(List.of(),
        () -> {
            List<T> selectedItems = new ArrayList<>(contentPanel.getComponents().size());
            for (DLListBoxItem<?> itm : contentPanel.getComponentsOfType(DLListBoxItem.class, true)) {
                if (itm.selected.get()) {
                    selectedItems.add((T)itm.item);
                }
            }
            return Collections.unmodifiableList(selectedItems);
        }, (selectedItems) -> {
            boolean prevLoop = loopFix;
            loopFix = true;
            try {
                for (DLListBoxItem<?> itm : contentPanel.getComponentsOfType(DLListBoxItem.class, true)) {
                    itm.selected.set(selectedItems.contains(itm.item));
                }
            } finally {
                loopFix = prevLoop;
            }
        });

    public final Property<Function<T, FormattedText>> textFormat = new Property<Function<T, FormattedText>>((item) -> TextUtils.text(String.valueOf(item)))
        .withAfterPropertyChangedCallback((a, b) -> {
            createComponents();
        });

    protected final DLScrollBar scrollBar;
    private DLCollectionItem<?, ?> previouslyClickedItem;


    public DLItemSelectionBox(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.scrollBar = new DLScrollBar(width() - 1 - 7, 1, 7, height() - 2, Orientation.VERTICAL);
        scrollBar.componentRenderer.set(VanillaListScrollBarRenderer.VANILLA_SCROLLBAR);
        scrollBar.inputConsumptionPolicy.set((type) -> true);
        scrollBar.anchor.set2(EAlign.TOP, EAlign.BOTTOM, EAlign.RIGHT);
        addComponent(scrollBar);

        this.contentPanel.setPosition(1, 1);
        this.contentPanel.setSize(width() - 2 - scrollBar.width(), height() - 2);

        this.contentPanel.addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
            layoutComponents(e.layoutResult());
            return false;
        });

        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (src, event) -> {
            contentPanel.setScrollOffsetY(scrollBar.value.get());
            return false;
        });

        addEventListener(DLGuiStandardEvents.ScrollEvent.class, scrollBar::invokeEvent);
        addEventListener(DLGuiStandardEvents.KeyPressEvent.class, (src, event) -> {
            if (DLWindowManager.isSelectAll(event.keyCode())) {
                selectAll();
            }
            return false;
        });

        addEventListener(ItemSelectionChangeEvent.class, (src, event) -> {
            if (loopFix) return false;
            loopFix = true;
            try {
                defaultItemSelection(event);
            } finally {
                loopFix = false;
            }
            return false;
        });
    }

    protected void selectAll() {
        boolean prevLoop = loopFix;
        loopFix = true;
        try {
            for (DLListBoxItem<?> itm : contentPanel.getComponentsOfType(DLListBoxItem.class, true)) {
                itm.selected.set(true);
            }
        } finally {
            loopFix = prevLoop;
        }
    }
    
    protected void defaultItemSelection(ItemSelectionChangeEvent event) {
        DLListBoxItem<?> clickedItem = event.item();
        boolean ctrl = DLWindowManager.hasControlDown();
        boolean shift = DLWindowManager.hasShiftDown();

        List<DLListBoxItem<T>> allItems = contentPanel.getComponentsOfType(DLListBoxItem.class, true).stream().map(x -> (DLListBoxItem<T>)x).toList();

        if (!multiselect.get()) {
            for (DLListBoxItem<T> itm : allItems) {
                if (itm != clickedItem) itm.selected.set(false);
            }
            event.selected().setValue(true);
        } else {
            if (shift && previouslyClickedItem != null) {
                int idxA = Math.max(0, contentPanel.getComponents().indexOf(previouslyClickedItem));
                int idxB = Math.max(0, contentPanel.getComponents().indexOf(clickedItem));
                int start = Math.min(idxA, idxB);
                int end = Math.max(idxA, idxB);

                if (start < 0 || end < 0 || start >= contentPanel.getComponents().size() || end >= contentPanel.getComponents().size()) {
                    for (DLListBoxItem<T> itm : allItems) {
                        if (itm != clickedItem) itm.selected.set(false);
                    }
                    event.selected().setValue(true);
                } else {
                    for (int i = start; i <= end; i++) {
                        var comp = contentPanel.getComponents().get(i);
                        if (comp instanceof DLListBoxItem<?> itm) {
                            itm.selected.set(true);
                        }
                    }
                    event.selected().setValue(true);
                }
            } else if (ctrl) {
                event.selected().setValue(!clickedItem.selected.get());
            } else {
                for (DLListBoxItem<T> itm : allItems) {
                    if (itm != clickedItem) itm.selected.set(false);
                }
                event.selected().setValue(true);
            }
        }

        previouslyClickedItem = clickedItem;
    }

    protected List<DLListBoxItem<T>> getSelectedComponent() {
        return contentPanel.getComponentsOfType(DLListBoxItem.class, true).stream().filter(x -> x.selected.get()).map(x -> (DLListBoxItem<T>)x).toList();
    }

    @Override
    protected void createComponents() {
        super.createComponents();
        for (DLListBoxItem<?> itm : contentPanel.getComponentsOfType(DLListBoxItem.class, true)) {
            itm.addEventListener(DLGuiStandardEvents.KeyPressEvent.class, this::invokeEvent);
        }
    }

    protected void layoutComponents(LayoutResult result) {
        scrollBar.visible.set(result.causesOverflowY(contentPanel.height()));
        scrollBar.max.set(result.contentHeight());
        scrollBar.screenSize.set(contentPanel.height());
    }

    @Override
    protected DLListBoxItem<T> defaultItemBuilder(T item) {
        return new DLListBoxItem<>(this, item, width(), 16);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawBox(graphics, Rectangle.withSize(0, 0, width(), height()), DLColor.fromInt(0x66000000), DLColor.WHITE);
    }

    public static class DLListBoxItem<T> extends DLAbstractCollectionComponent.DLCollectionItem<T, DLItemSelectionBox<T>> {

        public final BooleanProperty selected = new BooleanProperty(false)
            .withModificationCallback((o, n) -> {
                MutableBoolean sel = new MutableBoolean(n);
                collectionComponentRef.invokeEvent((DLGuiComponent)collectionComponentRef, new ItemSelectionChangeEvent(this, sel));
                return sel.getValue();
            });

        protected DLListBoxItem(DLItemSelectionBox<T> collectionComponentRef, T item, int w, int h) {
            super(collectionComponentRef, item, w, h);

            addEventListener(DLGuiStandardEvents.ClickEvent.class, (src, event) -> {
                this.selected.set(true);
                return false;
            });
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            if (selected.get()) {
                GuiUtils.fill(graphics, 0, 0, width(), height(), DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR);
                GuiUtils.fill(graphics, 1, 1, width() - 2, height() - 2, DLColor.BLACK);
            } else if (isSelected()) {
                GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0x22FFFFFF));
            }
            GuiUtils.drawString(graphics, Minecraft.getInstance().font, 4, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, collectionComponentRef.textFormat.get().apply(item), selected.get() ? DragonLib.VANILLA_BUTTON_HIGHLIGHTED_FONT_COLOR : DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        }

    }
}
