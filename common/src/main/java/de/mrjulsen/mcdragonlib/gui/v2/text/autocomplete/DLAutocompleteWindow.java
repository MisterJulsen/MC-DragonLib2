package de.mrjulsen.mcdragonlib.gui.v2.text.autocomplete;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.gui.v2.window.DLWindowManager;
import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.layout.BorderLayout;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.widget.DLScrollBar;
import de.mrjulsen.mcdragonlib.gui.v2.widget.DLTextBox;
import de.mrjulsen.mcdragonlib.gui.v2.window.DLWindow;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

/**
 * The dropdown a {@link DLTextBox} shows its completion suggestions in. Built and filled by an
 * {@link IAutocompletionManager}, which the text box's {@code autocompleteManager} property holds.
 *
 * <p>It is a top level window rather than a child of the text box, so it is never clipped by it. It
 * listens on the text box for the arrow keys, Tab and Enter and closes itself once neither it nor the
 * text box has the focus.</p>
 */
public class DLAutocompleteWindow<T> extends DLWindow {

    /**
     * The pane the suggestions are drawn in. Only exists to give the list its own scroll offset.
     */
    public static final class ListPane extends GuiComponent {

        public ListPane() {
            super(0, 0, 1, 1);
        }
    }

    protected static final int ITEM_HEIGHT = 12;
    protected static final int ITEMS_PER_PAGE = 10;
    protected static final int SCROLL_BAR_WIDTH = 7;
    protected static final int BORDER = 1;

    /**
     * Priority high enough that the suggestion list sees the arrow keys before the text box moves its
     * caret with them.
     */
    protected static final int KEY_PRIORITY = GuiEvent.PRIORITY_HIGH;

    private final Component txtNoResults = TextUtils.translate("gui." + DragonLib.MODID + ".autocomplete.no_results").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private final DLTextBox parentComponent;
    boolean supressTextUpdate;

    private final EventListenerId keyEventId;
    private final EventListenerId focusEventId;

    public final ListProperty<T> items = new ListProperty<T>().withAfterPropertyChangedCallback((o, n) -> applyFilter());

    public final Property<Predicate<T>> itemFilter = new Property<Predicate<T>>(i -> true)
            .withAfterPropertyChangedCallback((o, n) -> applyFilter());

    public final Property<Function<T, FormattedText>> textFormat = new Property<Function<T, FormattedText>>(
            (item) -> TextUtils.text(String.valueOf(item))).withAfterPropertyChangedCallback((o, n) -> applyFilter());

    private int selectedIndex = 0;
    private int firstVisible = 0;
    private final List<T> filteredItems = new LinkedList<>();

    private final ListPane contentPane;
    private final DLScrollBar scrollBar;

    public DLAutocompleteWindow(DLTextBox parentComponent) {
        super(parentComponent.width(), ITEM_HEIGHT * ITEMS_PER_PAGE + BORDER * 2);
        this.parentComponent = parentComponent;
        spawnPosition.set(SpawnPosition.CUSTOM);
        topLevel.set(true);
        closeOnEscape.set(true);

        BorderLayout layout = new BorderLayout();
        padding.set(new Insets(BORDER));
        this.layout.set(layout);

        scrollBar = new DLScrollBar(0, 0, SCROLL_BAR_WIDTH, height(), DLScrollBar.Orientation.VERTICAL);
        scrollBar.layoutConstraint.set(BorderLayout.Region.RIGHT);
        scrollBar.overrideThemeKey(DLScrollBar.THEME_KEY_RENDERER, DLScrollBar.Renderer.SIMPLE);
        scrollBar.stepSize.set(1);
        scrollBar.screenSize.set(ITEMS_PER_PAGE);
        addComponent(scrollBar);

        contentPane = new ListPane();
        contentPane.layoutConstraint.set(BorderLayout.Region.CENTER);
        addComponent(contentPane);

        contentPane.addEventListener(GuiEvents.RenderEvent.class, (s, e) -> {
            renderItems(e.graphics(), s);
            e.consume();
        });
        contentPane.addEventListener(GuiEvents.MouseMoveEvent.class, (s, e) -> {
            selectedIndex = (int) (Math.max(firstVisibleIndex(), 0) + (e.localY(s) / ITEM_HEIGHT));
            e.consume();
        });

        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> firstVisible = (int) e.value());

        keyEventId = parentComponent.addEventListener(GuiEvents.KeyPressEvent.class, (s, e) -> {
            switch (e.keyCode()) {
                case GLFW.GLFW_KEY_UP -> {
                    changeIndex(-1);
                    e.consume();
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    changeIndex(1);
                    e.consume();
                }
                case GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_ENTER -> {
                    closeAndSetValue();
                    e.consume();
                }
                default -> {
                }
            }
        }, KEY_PRIORITY);

        focusEventId = parentComponent.addEventListener(GuiEvents.FocusChangedEvent.class, (s, e) -> {
            if (!e.focused() && !isActive()) {
                close();
            }
        });

        addEventListener(WindowActivationEvent.class, (src, e) -> {
            DLWindowManager manager = getWindowManager();
            if (manager == null) {
                return;
            }
            manager.invokeLater(() -> {
                if (!e.active() && !parentComponent.isFocused()) {
                    close();
                }
            });
        });

        contentPane.addEventListener(GuiEvents.ClickEvent.class, (s, e) -> {
            closeAndSetValue();
            e.consume();
        });
        contentPane.addEventListener(GuiEvents.ScrollEvent.class, (s, e) -> scrollBar.invokeEvent(scrollBar, e));

        applyFilter();
    }

    /**
     * Places this window right below the text box it belongs to.
     */
    public void positionBelowParent() {
        setPosition(
            parentComponent.getTransform().screenX(),
            parentComponent.getTransform().screenBottom() + 2
        );
    }

    public DLTextBox parentComponent() {
        return parentComponent;
    }

    public List<T> filteredItems() {
        return filteredItems;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    /**
     * The index of the topmost suggestion currently drawn, driven by the scrollbar.
     * <p>This is counted in items, not pixels, which is why it is kept here rather than as the pane's
     * own scroll offset.</p>
     */
    protected int firstVisibleIndex() {
        return Math.max(firstVisible, 0);
    }

    protected void renderItems(DLGuiGraphics g, GuiComponent pane) {
        int startIdx = Math.max(firstVisibleIndex(), 0);
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, filteredItems.size() - 1);
        int fontHeight = Minecraft.getInstance().font.lineHeight;

        if (selectedIndex >= startIdx && selectedIndex <= endIdx) {
            GuiUtils.fill(g, 0, (selectedIndex - startIdx) * ITEM_HEIGHT, pane.dWidth(), ITEM_HEIGHT,
                    DLColor.fromInt(0x40FFFFFF));
        }

        if (filteredItems.isEmpty()) {
            GuiUtils.text(g, txtNoResults, 0, ITEM_HEIGHT / 2D - fontHeight / 2D, pane.dWidth(),
                    ETextAlignment.CENTER, DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR, false);
            return;
        }

        String searchText = parentComponent.getText();
        for (int i = startIdx; i <= endIdx && i < filteredItems.size(); i++) {
            GuiUtils.text(g, highlighted(filteredItems.get(i), searchText), 2,
                    ((i - startIdx) * ITEM_HEIGHT) + (ITEM_HEIGHT / 2D - fontHeight / 2D),
                    DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, false);
        }
    }

    /**
     * The item's label with the part matching what was typed so far picked out in yellow.
     */
    protected MutableComponent highlighted(T item, String searchText) {
        String itemName = textFormat.get().apply(item).getString();
        MutableComponent txt = TextUtils.empty();
        int index = searchText.isEmpty() ? -1 : itemName.toLowerCase().indexOf(searchText.toLowerCase());
        if (index == -1) {
            return txt.append(TextUtils.text(itemName));
        }
        return txt.append(TextUtils.text(itemName.substring(0, index)))
                .append(TextUtils.text(itemName.substring(index, index + searchText.length())).withStyle(ChatFormatting.YELLOW))
                .append(TextUtils.text(itemName.substring(index + searchText.length())));
    }

    protected void applyFilter() {
        filteredItems.clear();
        for (T item : items.get()) {
            if (itemFilter.get().test(item)) {
                filteredItems.add(item);
            }
        }
        selectedIndex = 0;
        firstVisible = 0;
        scrollBar.value.set(0D);
        scrollBar.max.set(filteredItems.size());
        setHeight(MathUtils.clamp(filteredItems.size(), 1, ITEMS_PER_PAGE) * ITEM_HEIGHT + BORDER * 2);
    }

    private void changeIndex(int direction) {
        selectedIndex = MathUtils.clamp(selectedIndex + direction, 0, filteredItems.size() - 1);
        int startIdx = Math.max(firstVisibleIndex(), 0);
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE - 1, filteredItems.size() - 1);

        if (selectedIndex < startIdx) {
            scrollBar.value.set((double) selectedIndex);
        } else if (selectedIndex > endIdx) {
            scrollBar.value.set((double) (selectedIndex - ITEMS_PER_PAGE + 1));
        }
    }

    private void closeAndSetValue() {
        supressTextUpdate = true;
        if (!filteredItems.isEmpty() && selectedIndex < filteredItems.size() && selectedIndex >= 0) {
            parentComponent.setText(textFormat.get().apply(filteredItems.get(selectedIndex)).getString());
            parentComponent.moveCaretTo(parentComponent.document().length(), false);
        }
        close();
        supressTextUpdate = false;
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.box(g, 0, 0, dWidth(), dHeight(), DLColor.fromInt(0xDD000000), DLColor.fromInt(0xFFDBDBDB));
    }

    @Override
    protected void onClosed() {
        parentComponent.removeEventListener(GuiEvents.KeyPressEvent.class, keyEventId);
        parentComponent.removeEventListener(GuiEvents.FocusChangedEvent.class, focusEventId);
        super.onClosed();
    }

    public boolean supressTextUpdate() {
        return supressTextUpdate;
    }
}
