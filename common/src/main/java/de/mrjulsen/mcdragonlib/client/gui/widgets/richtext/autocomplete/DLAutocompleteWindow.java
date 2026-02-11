package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.autocomplete;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.BorderLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaListScrollBarRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Point;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import de.mrjulsen.mcdragonlib.util.properties.VirtualProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class DLAutocompleteWindow<T> extends DLWindow {

    private final int ITEM_HEIGHT = 12;
    private final int ITEMS_PER_PAGE = 10;

    private final Component txtNoResults = TextUtils.translate("gui." + DragonLib.MODID + ".autocomplete.no_results").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private final DLRichTextEditBox parentComponent;
    boolean supressTextUpdate;

    private final EventListenerId keyEventId;
    private final EventListenerId focusEventId;

    public final ListProperty<T> items = new ListProperty<T>().withAfterPropertyChangedCallback((o, n) -> {
        applyFilter();
    });

    public final Property<Predicate<T>> itemFilter = new Property<Predicate<T>>(i -> true).withAfterPropertyChangedCallback((o, n) -> {
        applyFilter();
    });
    public final Property<Function<T, FormattedText>> textFormat = new Property<Function<T, FormattedText>>((item) -> TextUtils.text(String.valueOf(item))).withAfterPropertyChangedCallback((o, n) -> {
        applyFilter();
    });;

    @Deprecated(forRemoval = true)
    public final VirtualProperty<List<T>> suggestions;
    @Deprecated(forRemoval = true)
    public final VirtualProperty<Predicate<T>> filter;

    private int selectedIndex = 0;
    private final List<T> filteredItems = new LinkedList<>();

    private final DLPanel contentPane;
    private final DLScrollBar scrollBar;


    public DLAutocompleteWindow(DLWindowManager manager, DLRichTextEditBox parentComponent) {
        super(manager);
        this.parentComponent = parentComponent;
        Point pos = parentComponent.toScreenCoordinates();
        setPosition(pos.x(), pos.y() + parentComponent.height() + 2);
        setSize(parentComponent.width(), ITEM_HEIGHT * ITEMS_PER_PAGE + 2);
        inputConsumptionPolicy.set(p -> true);
        topLevel.set(true);

        this.suggestions = new VirtualProperty<>(List.of(), items::get, items::set);
        this.filter = new VirtualProperty<>(i -> true, itemFilter::get, itemFilter::set);

        BorderLayout layout = new BorderLayout(0, 0);
        layout.setPadding(new Padding(1));
        this.layout.set(layout);

        scrollBar = new DLScrollBar(0, 0, 7, height(), DLScrollBar.Orientation.VERTICAL);
        scrollBar.layoutContraint.set(BorderLayout.BorderPosition.EAST);
        scrollBar.scrollSteps.set(1);
        scrollBar.scrollerSize.set(-1);
        scrollBar.screenSize.set(ITEMS_PER_PAGE);
        scrollBar.componentRenderer.set(VanillaListScrollBarRenderer.VANILLA_SCROLLBAR);
        addComponent(scrollBar);

        contentPane = new DLPanel(0, 0, 1, 1);
        contentPane.layoutContraint.set(BorderLayout.BorderPosition.CENTER);
        addComponent(contentPane);

        contentPane.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                int startIdx = (int)Math.max(contentPane.getScrollOffsetY(), 0);
                int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, filteredItems.size() - 1);

                if (selectedIndex >= startIdx && selectedIndex <= endIdx) {
                    GuiUtils.fill(e.graphics(), 0, (selectedIndex - startIdx) * ITEM_HEIGHT, s.width(), ITEM_HEIGHT, DLColor.fromInt(0x40FFFFFF));
                }

                if (filteredItems.isEmpty()) {
                    GuiUtils.drawString(e.graphics(), e.graphics().defaultFont(), width() / 2, (ITEM_HEIGHT / 2 - Minecraft.getInstance().font.lineHeight / 2), txtNoResults, DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR, ETextAlignment.CENTER, false);
                }

                String searchText = parentComponent.text.get().getPlainText();
                for (int i = startIdx; i <= endIdx && i < filteredItems.size(); i++) {
                    T item = filteredItems.get(i);
                    String itemName = textFormat.get().apply(item).getString();
                    int index = itemName.toLowerCase().indexOf(searchText.toLowerCase());
                    MutableComponent txt = TextUtils.empty();
                    if (index != -1) {
                        String before = itemName.substring(0, index);
                        String match = itemName.substring(index, index + searchText.length());
                        String after = itemName.substring(index + searchText.length());
                        txt = txt.append(TextUtils.text(before)).append(TextUtils.text(match).withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(after));
                    }
                    GuiUtils.drawString(e.graphics(), e.graphics().defaultFont(), 2, ((i - startIdx) * ITEM_HEIGHT) + (ITEM_HEIGHT / 2 - Minecraft.getInstance().font.lineHeight / 2), txt, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
                }
            }
            return true;
        });
        contentPane.addEventListener(DLGuiStandardEvents.MouseMoveEvent.class, (s, e) -> {
            selectedIndex = (int)(Math.max(contentPane.getScrollOffsetY(), 0) + (e.mouseY() / ITEM_HEIGHT));
            return true;
        });

        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            contentPane.setScrollOffsetY((int)e.value());
            return false;
        });

        keyEventId = parentComponent.addEventListener(DLGuiStandardEvents.KeyPressEvent.class, (s, e) -> {
            switch (e.keyCode()) {
                case GLFW.GLFW_KEY_UP:
                    changeIndex(-1);
                    return true;
                case GLFW.GLFW_KEY_DOWN:
                    changeIndex(1);
                    return true;
                case GLFW.GLFW_KEY_TAB:
                case GLFW.GLFW_KEY_ENTER:
                    closeAndSetValue();
                    return true;
            };
            return false;
        }, 100);
        
        focusEventId = parentComponent.addEventListener(DLGuiStandardEvents.FocusChangedEvent.class, (s, e) -> {
            if (getWindowManager().getFocusedWindow() != this && !e.focus()) {
                getWindowManager().closeWindow(this);
            }
            return false;
        });
        
        addEventListener(WindowFocusEvent.class, (src, e) -> {
            getWindowManager().invokeLater(() -> {
                if (!e.focus() && !parentComponent.isFocused()) {
                    getWindowManager().closeWindow(this);
                }
            });
            return false;
        });

        contentPane.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            closeAndSetValue();
            return true;
        });
        contentPane.addEventListener(DLGuiStandardEvents.ScrollEvent.class, scrollBar::invokeEvent);

        applyFilter();
    }

    protected void applyFilter() {
        filteredItems.clear();
        for (T item : items.get()) {
            if (filter.get().test(item)) {
                filteredItems.add(item);
            }
        }
        selectedIndex = 0;
        scrollBar.max.set(filteredItems.size());
        setHeight(MathUtils.clamp(filteredItems.size(), 1, ITEMS_PER_PAGE) * ITEM_HEIGHT + 2);
    }

    private void changeIndex(int direction) {
        selectedIndex = MathUtils.clamp(selectedIndex + direction, 0, filteredItems.size() - 1);
        int startIdx = (int)Math.max(contentPane.getScrollOffsetY(), 0);
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE - 1, filteredItems.size() - 1);

        if (selectedIndex < startIdx) {
            scrollBar.value.set((double)selectedIndex);
        } else if (selectedIndex > endIdx) {
            scrollBar.value.set((double)(selectedIndex - ITEMS_PER_PAGE + 1));
        }
    }

    private void closeAndSetValue() {
        supressTextUpdate = true;
        if (!filteredItems.isEmpty() && selectedIndex < filteredItems.size() && selectedIndex >= 0) {
            parentComponent.text.get().set(textFormat.get().apply(filteredItems.get(selectedIndex)).getString());
        }
        getWindowManager().closeWindow(this);
        supressTextUpdate = false;
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawBox(graphics, 0, 0, width(), height(), DLColor.fromInt(0xDD000000), DLColor.fromInt(0xFFDBDBDB));
    }

    @Override
    public void close() throws Exception {
        this.parentComponent.removeEventListener(DLGuiStandardEvents.KeyPressEvent.class, keyEventId);
        this.parentComponent.removeEventListener(DLGuiStandardEvents.FocusChangedEvent.class, focusEventId);
        super.close();
    }

    public boolean supressTextUpdate() {
        return supressTextUpdate;
    }
    
}
