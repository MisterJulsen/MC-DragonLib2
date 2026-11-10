package de.mrjulsen.mcdragonlib.client.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.SizeMode;
import de.mrjulsen.mcdragonlib.client.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.BoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.client.gui.v2.window.DLPopup;
import de.mrjulsen.mcdragonlib.client.gui.v2.window.DLWindowManager;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class DLComboBox<T> extends GuiComponent {

    public static class Renderer implements WidgetRenderer<DLComboBox<?>> {

        public static final Renderer STANDARD = new Renderer(DLTextureSheet.VANILLA_BUTTON);

        public static final String SPRITE_NORMAL = "normal";
        public static final String SPRITE_PRESSED = "down_selected";
        public static final String SPRITE_HOVERED = "selected";
        public static final String SPRITE_DISABLED = "disabled";
        public static final String SPRITE_DISABLED_SELECTED = "disabled_selected";

        protected final DLTextureSheet spriteSheet;

        public Renderer(DLTextureSheet spriteSheet) {
            this.spriteSheet = spriteSheet;
        }

        @Override
        public void render(DLGuiGraphics g, DLComboBox<?> component, double x, double y, double width, double height) {
            if (component.enabled.get() && (component.isHovered() || component.isFocused())) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_DISABLED_SELECTED), x, y, width - BUTTON_WIDTH + 1, height);
            } else {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_DISABLED), x, y, width - BUTTON_WIDTH + 1, height);
            }

            double btnX = x + width - BUTTON_WIDTH;
            if (!component.enabled.get()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_DISABLED), btnX, y, BUTTON_WIDTH, height);
            } else if (component.isPressed()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_PRESSED), btnX, y, BUTTON_WIDTH, height);
            } else if (component.isHovered()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_HOVERED), btnX, y, BUTTON_WIDTH, height);
            } else {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_NORMAL), btnX, y, BUTTON_WIDTH, height);
            }
            int offset = component.isPressed() ? 1 : 0;
            GuiIcons.ARROW_DOWN.render(g.legacy(), (int)(btnX + (double)(BUTTON_WIDTH - GuiIcons.ICON_SIZE) / 2), (int)(height - GuiIcons.ICON_SIZE) / 2 + offset);
        }
    }

    private static final int BUTTON_WIDTH = 16;
    private static final int TEXT_GAP = 3;
    private static final int DEFAULT_VISIBLE_ITEMS_COUNT = 12;
    public static final int INVALID = -1;

    public static final Theme.Key<WidgetRenderer<DLComboBox<?>>> THEME_KEY_RENDERER = Theme.key(DragonLib.MODID + ".combobox.renderer", Renderer.STANDARD);
    public static final Theme.Key<DLColor> THEME_KEY_BACKGROUND_COLOR = Theme.key(DragonLib.MODID + ".combobox.background_color", DLColor.WHITE);

    public final ListProperty<T> items = new ListProperty<T>().withUpdateCallback((val, operation) -> updateSelectedItem(operation == ListProperty.ListOperation.ADD || operation == ListProperty.ListOperation.REPLACE));
    public final Property<Optional<T>> selectedItem = new Property<>(Optional.empty());
    public final NumberProperty<Integer> selectedIndex = new NumberProperty<>(INVALID, INVALID, Integer.MAX_VALUE);
    public final Property<Function<Optional<T>, Component>> textFormat = new Property<>((item) -> TextUtils.text(item.map(Object::toString).orElse("")));
    public final NumberProperty<Integer> shiftStep = new NumberProperty<>(5, 1, Integer.MAX_VALUE);
    public final NumberProperty<Integer> maxVisibleItems = new NumberProperty<>(DEFAULT_VISIBLE_ITEMS_COUNT, 1, Integer.MAX_VALUE);
    public final BooleanProperty drawFontShadow = new BooleanProperty(true);


    public DLComboBox(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.textFormat.addChangeListener((o, n) -> {
            invalidateLayout();
            invokeEvent(this, new DLItemPicker.TextFormatChangedEvent());
        });
        this.selectedIndex.addChangeListener((o, n) -> {
            if (o.equals(n)) {
                return;
            }
            int idx = n;
            if (n < 0 || n >= items.size()) {
                idx = INVALID;
            }
            Optional<T> item = Optional.ofNullable(idx == INVALID ? null : items.get(n));
            selectedItem.set(item);
            invokeEvent(this, new DLItemPicker.SelectedItemChanged<>(item, n));
        });
        this.selectedItem.addChangeListener((o, n) -> {
            if (!Objects.equals(o.orElse(null), n.orElse(null))) {
                selectedIndex.set(n.map(items::indexOf).orElse(INVALID));
            }
        });
        addEventListener(GuiEvents.ScrollEvent.class, (src, event) -> {
            changeValueOnScroll((int)Math.signum(event.deltaY()));
        });
        addEventListener(GuiEvents.ClickEvent.class, (src, event) -> openDropdown());
    }

    protected void openDropdown() {
        DLWindowManager manager = getWindowManager();
        if (manager == null || items.isEmpty()) {
            return;
        }

        DLListBox<T> list = new DLListBox<>(0, 0, width(), 0);
        list.heightMode.set(SizeMode.AUTO);
        list.textFormat.set(item -> textFormat.get().apply(Optional.ofNullable(item)));
        list.items.addAll(items);
        list.allowNoSelection.set(false);
        list.overrideThemeKey(DLListBox.THEME_KEY_COLOR, new BoxStyle(DLColor.fromInt(0xFF666666), DLColor.fromInt(0xFF444444)));
        list.overrideThemeKey(DLListBox.THEME_KEY_ITEM_HOVER_COLOR, new BoxStyle(DLColor.fromInt(0x44FFFFFF)));
        list.overrideThemeKey(DLListBox.THEME_KEY_ITEM_SELECTION_COLOR, new BoxStyle(DLColor.WHITE, DLColor.TRANSPARENT));
        if (selectedIndex.get() != INVALID) {
            list.selectedIndices.set(List.of(selectedIndex.get()));
        }

        int required = Math.min(items.size(), maxVisibleItems.get());
        int below = list.itemsFitting(manager.screenHeight() - getTransform().screenBottom());
        int above = list.itemsFitting(getTransform().screenY());
        boolean down = below >= required || below >= above;
        list.maxVisibleItems.set(Math.max(1, Math.min(required, down ? below : above)));

        DLPopup popup = new DLPopup(list);
        list.addEventListener(DLListBox.ItemActivation.class, (s, e) -> {
            if (e.index() != INVALID) {
                selectedIndex.set(e.index());
            }
            popup.closeCascade();
        });
        popup.openRelativeTo(this, down ? DLPopup.Placement.BELOW : DLPopup.Placement.ABOVE, 0, 0);
        list.scrollToItem(selectedIndex.get());
    }

    protected void updateSelectedItem(boolean firstIfUnselected) {
        if (selectedItem.get().isPresent() && this.items.contains(selectedItem.get().get())) {
            this.selectedIndex.set(this.items.indexOf(selectedItem.get().get()));
        } else {
            this.selectedIndex.set(firstIfUnselected ? 0 : INVALID);
        }
    }

    public void changeValueOnScroll(int direction) {
        this.selectedIndex.set(MathUtils.clamp(this.selectedIndex.get() + direction * (Screen.hasShiftDown() ? shiftStep.get() : 1), 0, items.size() - 1));
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.withTint(getThemeValue(THEME_KEY_BACKGROUND_COLOR), () -> {
            getThemeValue(THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight());
        });

        g.pushClip(this, 0, 0, width() - BUTTON_WIDTH - TEXT_GAP, height());
        GuiUtils.text(g, textFormat.get().apply(selectedItem.get()), TEXT_GAP + 1, (double)height() / 2 - (double) Minecraft.getInstance().font.lineHeight / 2, getThemeValue(!enabled.get() ? Theme.TEXT_DISABLED : Theme.TEXT), drawFontShadow.get());
        g.popClip();
    }
}
