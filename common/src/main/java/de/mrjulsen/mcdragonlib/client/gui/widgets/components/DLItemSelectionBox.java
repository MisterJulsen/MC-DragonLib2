package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaListScrollBarRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.VirtualProperty;
import net.minecraft.client.Minecraft;

@SupportsEvents({
    DLItemSelectionBox.ItemSelectionChangeEvent.class,
})
public class DLItemSelectionBox<T> extends DLAbstractCollectionComponent<T, DLItemSelectionBox.DLListBoxItem<T>> {
    
    public record ItemSelectionChangeEvent<E extends DLCollectionItem<?, ?>>(E item, MutableBoolean selected) implements IEvent {}


    public static class DLListBoxItem<T> extends DLAbstractCollectionComponent.DLCollectionItem<T, DLItemSelectionBox<T>> {

        public final BooleanProperty selected = new BooleanProperty(false, false);

        protected DLListBoxItem(DLItemSelectionBox<T> collectionComponentRef, T item, int w, int h) {
            super(collectionComponentRef, item, w, h);

            addEventListener(DLGuiStandardEvents.ClickEvent.class, (src, event) -> {
                MutableBoolean select = new MutableBoolean(this.selected.get());
                collectionComponentRef.invokeEvent((DLGuiComponent)collectionComponentRef, new ItemSelectionChangeEvent<DLCollectionItem<T, ?>>(this, select));
                this.selected.set(select.getValue());
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
            GuiUtils.drawString(graphics, Minecraft.getInstance().font, 4, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, String.valueOf(item), selected.get() ? DragonLib.VANILLA_BUTTON_HIGHLIGHTED_FONT_COLOR : DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        }

    }

    public final BooleanProperty multiselect = new BooleanProperty(false, false);

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
            for (DLListBoxItem<?> itm : contentPanel.getComponentsOfType(DLListBoxItem.class, true)) {
                itm.selected.set(selectedItems.contains(itm.item));
            }
        });

    private final DLScrollBar scrollBar;
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

        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (src, event) -> {
            contentPanel.setScrollOffsetY(scrollBar.value.get());
            return false;
        });

        addEventListener(DLGuiStandardEvents.ScrollEvent.class, scrollBar::invokeEvent);
        addEventListener(DLGuiStandardEvents.KeyPressEvent.class, (src, event) -> {
            if (DLWindowManager.isSelectAll(event.keyCode())) {
                for (DLListBoxItem<?> itm : contentPanel.getComponentsOfType(DLListBoxItem.class, true)) {
                    itm.selected.set(true);
                }
            }
            return false;
        });
        addEventListener(ItemSelectionChangeEvent.class, (src, event) -> {
            List<DLListBoxItem<T>> itemComponents = getSelectedComponent();
            boolean isSelected = event.selected().getValue();
            
            if (multiselect.get() && DLWindowManager.hasShiftDown()) {
                int a = previouslyClickedItem == null ? 0 : Math.max(0, contentPanel.getComponents().indexOf(previouslyClickedItem));
                int b = Math.max(0, contentPanel.getComponents().indexOf(event.item()));
                int startIdx = Math.min(a, b);
                int endIdx = Math.max(a, b);
                for (int i = startIdx; i < endIdx; i++) {
                    if (contentPanel.getComponents().get(i) instanceof DLListBoxItem itm) {
                        itm.selected.set(true);
                    }
                }
            }            

            if (!multiselect.get() || (!DLWindowManager.hasControlDown() && !DLWindowManager.hasShiftDown())) {
                for (DLListBoxItem<T> itm : itemComponents) {
                    if (itm != event.item()) {
                        itm.selected.set(false);
                    }
                }
            }

            previouslyClickedItem = event.item();

            if (isSelected && getSelectedComponent().size() <= 1) {
                return false;
            }
            event.selected().setValue(!isSelected);
            return false;
        });
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

    
    @SuppressWarnings("unchecked")
    @Override
    protected void layoutComponents() {
        int currentY = 0;
        for (DLListBoxItem<?> itm : contentPanel.getComponentsOfType(DLListBoxItem.class, true)) {
            DLListBoxItem<T> item = (DLListBoxItem<T>)itm;
            setItemX(item, 0);
            setItemY(item, currentY);
            setItemWidth(item, contentPanel.width());
            currentY += item.height();
        }
        int maxScroll = currentY - contentPanel.height();
        scrollBar.visible.set(maxScroll > 0);
        scrollBar.max.set(maxScroll);
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
}
