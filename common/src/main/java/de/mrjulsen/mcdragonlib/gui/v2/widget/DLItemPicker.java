package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.Alignment;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.SizeMode;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.input.InputKind;
import de.mrjulsen.mcdragonlib.gui.v2.layout.StackLayout;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetState;
import de.mrjulsen.mcdragonlib.gui.v2.widget.contextmenu.AbstractContextMenuItem;
import de.mrjulsen.mcdragonlib.gui.v2.widget.contextmenu.ContextMenuItem;
import de.mrjulsen.mcdragonlib.gui.v2.widget.contextmenu.ContextMenuSeparator;
import de.mrjulsen.mcdragonlib.gui.v2.widget.contextmenu.DLContextMenu;
import de.mrjulsen.mcdragonlib.util.Clipboard;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class DLItemPicker<T> extends GuiComponent {

    public record TextFormatChangedEvent() implements IEvent {}
    public record SelectedItemChanged<T>(Optional<T> item, int index) implements IEvent {}

    public static class ContentPanel<T> extends DLPanel {

        private final DLItemPicker<T> container;

        public ContentPanel(DLItemPicker<T> container) {
            super(0, 0, 1, 1);
            this.container = container;
            acceptedInput.set(InputKind.allExcept(InputKind.SCROLL));
            focusable.set(true);
        }

        @Override
        protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
            GuiUtils.withTint(getThemeValue(THEME_KEY_BACKGROUND_COLOR), () -> {
                getThemeValue(THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight());
            });
            g.pushClip(this, 3, 0, width() - 6, height());
            GuiUtils.text(g, container.textFormat.get().apply(container.selectedItem.get()), 4, (double)height() / 2 - (double)Minecraft.getInstance().font.lineHeight / 2, getThemeValue(!enabled.get() ? Theme.TEXT_DISABLED : Theme.TEXT), container.drawFontShadow.get());
            g.popClip();
        }
    }

    public static class Renderer implements WidgetRenderer<ContentPanel<?>> {

        protected static final String SPRITE_SELECTED = "selected";
        protected static final String SPRITE_FOCUSED = "focused";
        protected static final String SPRITE_DISABLED = "disabled";
        protected static final String SPRITE_NORMAL = "normal";

        @Override
        public void render(DLGuiGraphics g, ContentPanel<?> component, double x, double y, double width, double height) {
            String sprite;
            if (!component.enabled.get()) {
                sprite = SPRITE_DISABLED;
            } else if (component.isFocused()) {
                sprite = SPRITE_FOCUSED;
            } else if (component.isHovered()) {
                sprite = SPRITE_SELECTED;
            } else {
                sprite = SPRITE_NORMAL;
            }
            GuiUtils.sprite(g, DLTextureSheet.VANILLA_TEXTBOX.getSprite(sprite), x, y, width, height);
        }
    }

    private static final int BUTTON_WIDTH = 16;
    public static final int INVALID = -1;

    public static final Renderer RENDERER = new Renderer();
    public static final Theme.Key<WidgetRenderer<ContentPanel<?>>> THEME_KEY_RENDERER = Theme.key(DragonLib.MODID + ".item_picker.renderer", RENDERER);
    public static final Theme.Key<DLColor> THEME_KEY_BACKGROUND_COLOR = Theme.key(DragonLib.MODID + ".item_picker.background_color", DLColor.WHITE);

    public final ListProperty<T> items = new ListProperty<T>().withUpdateCallback((val, operation) -> updateSelectedItem(operation == ListProperty.ListOperation.ADD));
    public final Property<Optional<T>> selectedItem = new Property<>(Optional.empty());
    public final NumberProperty<Integer> selectedIndex = new NumberProperty<>(INVALID, INVALID, Integer.MAX_VALUE);

    public final BooleanProperty showButtons = new BooleanProperty(true);
    public final Property<Function<Optional<T>, Component>> textFormat = new Property<>((item) -> TextUtils.text(item.map(Object::toString).orElse("")));
    public final NumberProperty<Integer> shiftStep = new NumberProperty<>(5, 1, Integer.MAX_VALUE);
    public final BooleanProperty drawFontShadow = new BooleanProperty(true);

    protected final DLPanel buttonPanel;
    protected final DLButton addBtn;
    protected final DLButton subBtn;

    public DLItemPicker(int x, int y, int w, int h) {
        super(x, y, w, h);
        overrideThemeKey(DLAbstractButton.THEME_KEY_RENDERER, DLAbstractButton.FLAT_GRAY_RENDERER);

        // Events and listeners
        this.textFormat.addChangeListener((o, n) -> {
            invalidateLayout();
            invokeEvent(this, new TextFormatChangedEvent());
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
            invokeEvent(this, new SelectedItemChanged<>(item, n));
        });
        this.selectedItem.addChangeListener((o, n) -> {
            if (!Objects.equals(o.orElse(null), n.orElse(null))) {
                selectedIndex.set(n.map(items::indexOf).orElse(INVALID));
            }
        });
        addEventListener(GuiEvents.ScrollEvent.class, (src, event) -> {
            changeValueOnScroll((int)Math.signum(event.deltaY()));
        });

        // GUI and layout
        StackLayout layout = new StackLayout(StackLayout.Direction.HORIZONTAL);
        layout.crossAlignment.set(Alignment.STRETCH);
        this.layout.set(layout);

        buttonPanel = new DLPanel(0, 0, BUTTON_WIDTH, h);
        buttonPanel.acceptedInput.set(InputKind.allExcept(InputKind.SCROLL));
        StackLayout buttonLayout = new StackLayout(StackLayout.Direction.VERTICAL);
        buttonLayout.crossAlignment.set(Alignment.STRETCH);
        buttonPanel.layout.set(buttonLayout);

        addBtn = createStepButton(GuiIcons.ARROW_UP, 1);
        buttonPanel.addComponent(addBtn);

        subBtn = createStepButton(GuiIcons.ARROW_DOWN, -1);
        buttonPanel.addComponent(subBtn);

        ContentPanel<T> content = new ContentPanel<>(this);
        content.widthMode.set(SizeMode.FILL);
        addComponent(content);
        addComponent(buttonPanel);
        
        updateButtons();
        this.showButtons.withAfterPropertyChangedCallback((o, val) -> {
            updateButtons();
        });

        content.addBehaviour(new DLContextMenu.Trigger(this::createContextMenu));
    }

    protected List<AbstractContextMenuItem> createContextMenu() {
        List<AbstractContextMenuItem> items = new ArrayList<>();
        items.add(new ContextMenuItem.Builder()
                .text(TextUtils.translate("gui." + DragonLib.MODID + ".menu.copy"))
                .enabled(() -> selectedIndex.get() >= 0)
                .clickAction((menu, localX, localY, button) -> {
                    selectedItem.get().ifPresent(i -> {
                        Minecraft.getInstance().keyboardHandler.setClipboard(i.toString());
                    });
                })
                .build());

        return items;
    }

    protected DLButton createStepButton(GuiIcons icon, int direction) {
        DLButton button = new DLButton(0, 0, BUTTON_WIDTH, 0);
        button.heightMode.set(SizeMode.FILL);
        button.text.set(TextUtils.empty());
        button.icon.set(new DLSprite(GuiIcons.ICON_TEXTURE, BUTTON_WIDTH, BUTTON_WIDTH, icon.getU(), icon.getV(), GuiIcons.ICON_SIZE, GuiIcons.ICON_SIZE));
        button.acceptedInput.set(InputKind.allExcept(InputKind.SCROLL));
        button.addEventListener(GuiEvents.MouseHoldDownEvent.class, (s, e) -> {
            if (e.leftClick()) {
                changeValueOnScroll(direction);
            }
        });
        return button;
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

    protected void updateButtons() {
        buttonPanel.visible.set(showButtons.get());
    }

    @Override
    protected Size measureContent(double availableWidth, double availableHeight) {
        Size content = super.measureContent(availableWidth, availableHeight);
        return showButtons.get() ? Size.of(content.w() + BUTTON_WIDTH, content.h()) : content;
    }
}
