package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.List;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents.ClickEvent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLPopupWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.ModalId;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaListScrollBarRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.ITextFormatter;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;

public class DLComboBox<T> extends DLCycleButton<T> {

    public static interface IComboBoxDropDownBuilder<T> {
        DLComboBoxDropDownList<T> build(ModalId id, int winWidth, int winHeight);
    }

    public static final int DROP_DOWN_BUTTON_WIDTH = 14;

    public final Property<ITextFormatter<DLComboBox<T>>> textFormat = new Property<>((src) -> TextUtils.text(src.selectedItem.get().map(x -> x.toString()).orElse(text.get().getString())).withStyle(src.text.get().getStyle()));
    public final NumberProperty<Integer> shiftStep = new NumberProperty<>(5, 1, Integer.MAX_VALUE);

    /**
     * Builds the dropdown menu that is displayed when the ComboBox is opened. This property can
     * be changed to use a custom dropdown list or a modified version of the default list without
     * having to change the dropdown logic itself.
     */
    public final Property<IComboBoxDropDownBuilder<T>> dropDownBuilder = new Property<>((id, winWidth, winHeight) -> new DLComboBoxDropDownList<>(this, id, 0, 0, winWidth, winHeight));


    public DLComboBox(int x, int y, int w, int h) {
        super(x, y, w, h);
        addEventListener(DLGuiStandardEvents.ScrollEvent.class, (src, event) -> {
            changeValueOnScroll(-(int)Math.signum(event.deltaY()));
            return false;
        });
    }

    @Override
    public boolean defaultButtonClickAction(DLGuiComponent src, ClickEvent event) {
        ModalId id = getWindowManager().createModal((root) -> {
            DLPopupWindow popup = new DLPopupWindow(root, (int)(getXOnScreen()), (int)(getYOnScreen() + height() * getGlobalScale()), width());
            return popup;
        });
        DLWindow win = getWindowManager().getWindows(id)[0];

        DLComboBoxDropDownList<T> list = dropDownBuilder.get().build(id, win.width(), win.height());
        list.items.addAll(items);
        list.anchor.set(EAlign.values());
        win.addComponent(list);
        win.setHeight(MathUtils.clamp(2 + list.getRequiredHeight(), 8, getWindowManager().getScreenHeight() / 3));
        win.setY(Math.min(win.y() + win.height(), getWindowManager().getScreenHeight()) - win.height());

        return false;
    }

    public void changeValueOnScroll(int direction) {
        this.selectedIndex.set(MathUtils.clamp(this.selectedIndex.get() + direction * (DLWindowManager.hasShiftDown() ? shiftStep.get() : 1), 0, items.size() - 1));
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        RenderSystem.enableBlend();
        DLWindowManager manager = getWindowManager();
        GuiUtils.setTint(backgroundTint.get());
        ButtonState backgroundState = ButtonState.DISABLED;
        if (isSelected()) {
            backgroundState = ButtonState.DISABLED_SELECTED;
        }
        componentRenderer.get().renderSprite(graphics, 0, 0, width() - 2, height(), this, backgroundState);

        ButtonState state = ButtonState.NORMAL;
        if (!enabled.get()) {
            state = ButtonState.DISABLED;
        } else if (isMouseDown() && (manager != null ? manager.getMouseDownButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT : true)) {
            state = ButtonState.DOWN_SELECTED;
        } else if (isSelected()) {
            state = ButtonState.SELECTED;
        }
        componentRenderer.get().renderSprite(graphics, width() - DROP_DOWN_BUTTON_WIDTH, 0, DROP_DOWN_BUTTON_WIDTH, height(), this, state);

        GuiUtils.setTint(textColor.get());
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, 4, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, textFormat.get().combine(this), enabled.get() ? DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR : DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiIcons.ARROW_DOWN.render(graphics, (width() - DROP_DOWN_BUTTON_WIDTH) + (DROP_DOWN_BUTTON_WIDTH / 2) - (GuiIcons.ICON_SIZE / 2) + (isMouseDown() ? 1 : 0), (height() / 2) - (GuiIcons.ICON_SIZE / 2) + (isMouseDown() ? 1 : 0));
        GuiUtils.resetTint();
    }



    public static class DLComboBoxDropDownList<T> extends DLAbstractCollectionComponent<T, DLComboBoxDropDownList.DLComboboxDropDownItem<T>> {

        protected final DLComboBox<T> combobox;
        protected final ModalId id;        
        protected final DLScrollBar scrollBar;
        
        protected int totalItemHeight = 0;

        public DLComboBoxDropDownList(DLComboBox<T> combobox, ModalId id, int x, int y, int w, int h) {
            super(x, y, w, h);
            this.id = id;
            this.combobox = combobox;
            
            this.scrollBar = new DLScrollBar(width() - 7, 0, 7, height(), Orientation.VERTICAL);
            scrollBar.componentRenderer.set(VanillaListScrollBarRenderer.VANILLA_SCROLLBAR);
            scrollBar.inputConsumptionPolicy.set((type) -> true);
            scrollBar.anchor.set2(EAlign.TOP, EAlign.BOTTOM, EAlign.RIGHT);
            addComponent(scrollBar);

            this.contentPanel.setPosition(1, 1);
            this.contentPanel.setSize(width() - scrollBar.width() - 2, height() - 2);

            scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (src, event) -> {
                contentPanel.setScrollOffsetY(scrollBar.value.get());
                return false;
            });
            addEventListener(DLGuiStandardEvents.ScrollEvent.class, scrollBar::invokeEvent);
        }

        public static class DLComboboxDropDownItem<T> extends DLAbstractCollectionComponent.DLCollectionItem<T, DLComboBoxDropDownList<T>> {

            protected final boolean selected;

            protected DLComboboxDropDownItem(DLComboBoxDropDownList<T> collectionComponentRef, T item, boolean selected, int w, int h) {
                super(collectionComponentRef, item, w, h);
                this.selected = selected;
                addEventListener(DLGuiStandardEvents.ClickEvent.class, (src, event) -> {
                    collectionComponentRef.combobox.selectedItem.set(Optional.ofNullable(item));
                    collectionComponentRef.closeMenu();
                    return false;
                });
            }

            @Override
            public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
                if (isSelected()) {
                    GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0x22FFFFFF));
                } else if (selected) {
                    GuiUtils.drawBox(graphics, Rectangle.withSize(0, 0, width(), height()), DLColor.TRANSPARENT, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR);
                }
                GuiUtils.drawString(graphics, Minecraft.getInstance().font, 4, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, String.valueOf(item), DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
            }

        }

        @SuppressWarnings("unchecked")
        @Override
        protected void layoutComponents() {
            int currentY = 0;
            for (DLComboboxDropDownItem<T> item : contentPanel.getComponentsOfType(DLComboboxDropDownItem.class, true)) {
                setItemWidth(item, width() - scrollBar.width() - 2);
                setItemX(item, 0);
                setItemY(item, currentY);
                currentY += item.height();
            }
            this.totalItemHeight = currentY;
            int maxScroll = currentY - contentPanel.height();
            scrollBar.visible.set(maxScroll > 0);
            scrollBar.max.set(maxScroll);
            scrollBar.screenSize.set(contentPanel.height());
        }

        @Override
        protected DLComboboxDropDownItem<T> defaultItemBuilder(T item) {
            return new DLComboboxDropDownItem<>(this, item, combobox.selectedItem.get().map(x -> x == item).orElse(false), width(), Minecraft.getInstance().font.lineHeight + 2);
        }

        public final void closeMenu() {
            if (getWindowManager() == null) return;
            getWindowManager().closeModal(id);
        }

        public int getRequiredHeight() {
            return totalItemHeight;
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0xFF666666));
            GuiUtils.fill(graphics, 1, 1, width() - 2, height() - 2, DLColor.fromInt(0xFF444444));
        }
    }
    
}
