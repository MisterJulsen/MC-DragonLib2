package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class DLAutocompleteListBox<T> extends DLItemSelectionBox<T> {
    
    private final DLAutocompleteWindow<T> windowRef;
    private final DLRichTextEditBox textbox;

    final List<T> visibleItems = new ArrayList<>();

    public DLAutocompleteListBox(DLAutocompleteWindow<T> windowRef, DLRichTextEditBox textbox, int x, int y, int w, int h) {
        super(x, y, w, h);
        this.windowRef = windowRef;
        this.textbox = textbox;
        this.contentPanel.setPosition(0, 0);
        this.contentPanel.setSize(width(), height());

        this.scrollBar.setPosition(width() - 7, 0);
        this.scrollBar.setSize(7, height());

        addEventListener(DLItemSelectionBox.ItemSelectionChangeEvent.class, (s, e) -> {
            if (e.selected().getValue()) {
                e.item().getParent().ifPresent(p -> p.scrollIntoView(e.item()));
            }
            return false;
        });        

        addEventListener(DLAbstractCollectionComponent.ListLayoutChangedEvent.class, (s, e) -> {
            //windowRef.setHeight(Math.max(e., h));
            return false;
        });

        layoutComponents();
    }

    @Override
    protected void createComponents() {
        contentPanel.clearComponents();
        visibleItems.clear();
        for (T item : items.get()) {
            if (!filter.get().test(item)) {
                continue;
            }
            visibleItems.add(item);
            DLListBoxItem<T> listItem = itemBuilder.get().apply(item);
            contentPanel.addComponent(listItem);
        }
    }

    @Override
    protected DLListBoxItem<T> defaultItemBuilder(T item) {
        return new AutocompleteItem<>(this, item);
    }

    public void selectFirst() {
        if (!visibleItems.isEmpty()) {
            selectedItems.set(List.of(visibleItems.get(0)));
        }
    }

    public int visibleCurrentIndex() {
        if (selectedItems.get().isEmpty()) {
            return 0;
        }
        return visibleIndexOf(selectedItems.get().get(0));
    }

    public int visibleIndexOf(T item) {
        return visibleItems.indexOf(item);
    }
    
    public void selectByVisibleIndex(int index) {
        selectedItems.set(List.of(visibleItems.get(index)));
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        
    }


    public static class AutocompleteItem<T> extends DLItemSelectionBox.DLListBoxItem<T> {

        protected AutocompleteItem(DLAutocompleteListBox<T> collectionComponentRef, T item) {
            super(collectionComponentRef, item, 100, Minecraft.getInstance().font.lineHeight + 2);

            addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                collectionComponentRef.windowRef.supressTextUpdate = true;
                collectionComponentRef.textbox.text.get().set(collectionComponentRef.textFormat.get().apply(item).getString());
                getWindowManager().closeWindow(collectionComponentRef.windowRef);
                collectionComponentRef.windowRef.supressTextUpdate = false;
                return false;
            });
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            if (selected.get()) {
                GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0x60FFFFFF));
            }
            
            if (isSelected()) {
                GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0x30FFFFFF));
            }
            DLAutocompleteListBox<T> list = (DLAutocompleteListBox<T>)collectionComponentRef;
            String plain = list.textbox.text.get().getPlainText();
            Component firstPart = TextUtils.text(plain).withStyle(ChatFormatting.YELLOW);
            Component secondPart = TextUtils.text(list.textFormat.get().apply(item).getString().replace(plain, ""));

            GuiUtils.drawString(graphics, Minecraft.getInstance().font, 2, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, TextUtils.empty().append(firstPart).append(secondPart), selected.get() ? DragonLib.VANILLA_BUTTON_HIGHLIGHTED_FONT_COLOR : DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        }

        public T getItem() {
            return item;
        }

    }
}
