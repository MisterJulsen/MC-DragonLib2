package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.autocomplete;

import java.util.ArrayList;
import java.util.List;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLAbstractCollectionComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLItemSelectionBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class DLAutocompleteListBox<T> extends DLItemSelectionBox<T> {


    private final Component txtNoResults = TextUtils.translate("gui." + DragonLib.MODID + ".autocomplete.no_results").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    
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
            windowRef.setHeight(Math.max(Math.min(e.layoutResult().contentHeight() + 2, 101), Minecraft.getInstance().font.lineHeight + 4));
            return false;
        });
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
        if (visibleItems.isEmpty()) {
            GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, height() / 2 - graphics.defaultFont().lineHeight / 2, txtNoResults, DLColor.WHITE, ETextAlignment.CENTER, false);
        }
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
            String searchText = list.textbox.text.get().getPlainText();
            String itemName = list.textFormat.get().apply(item).getString();            
            int index = itemName.toLowerCase().indexOf(searchText.toLowerCase());
            MutableComponent txt = TextUtils.empty();
            if (index != -1) {
                String before = itemName.substring(0, index);
                String match = itemName.substring(index, index + searchText.length());
                String after = itemName.substring(index + searchText.length());
                txt = txt.append(TextUtils.text(before)).append(TextUtils.text(match).withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(after));
            }

            GuiUtils.drawString(graphics, Minecraft.getInstance().font, 2, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, txt, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        }

        public T getItem() {
            return item;
        }

    }
}
