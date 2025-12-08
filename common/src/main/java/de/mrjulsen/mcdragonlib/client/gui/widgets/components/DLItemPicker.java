package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents.ClickEvent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.DLItemPickerRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.ITextFormatter;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;

public class DLItemPicker<T> extends DLCycleButton<T> {
    
    public static enum ItemPickerState {
        NORMAL,
        SELECTED,
        FOCUSED,
        DISABLED;
    }

    private static final int BUTTON_WIDTH = 16;

    protected final DLButton addBtn;
    protected final DLButton subBtn;

    public final BooleanProperty showButtons = new BooleanProperty(true);
    public final Property<ITextFormatter<DLItemPicker<T>>> textFormat = new Property<>((src) -> TextUtils.text(src.selectedItem.get().map(x -> x.toString()).orElse(text.get().getString())).withStyle(src.text.get().getStyle()));
    public final NumberProperty<Integer> shiftStep = new NumberProperty<>(5, 1, Integer.MAX_VALUE);
    public final BooleanProperty drawFontShadow = new BooleanProperty(true);

    public final Property<IStateRenderer<DLButton.ButtonState>> buttonsComponentRenderer = new Property<>(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
    public final Property<IStateRenderer<ItemPickerState>> componentRenderer = new Property<>(DLItemPickerRenderer.INSTANCE);

    public DLItemPicker(int x, int y, int w, int h) {
        super(x, y, w, h);
        
        addEventListener(DLGuiStandardEvents.ScrollEvent.class, (src, event) -> {
            changeValueOnScroll(-(int)Math.signum(event.deltaY()));
            return false;
        });

        addBtn = new DLButton(width() - BUTTON_WIDTH, 0, BUTTON_WIDTH, height() / 2);
        addBtn.icon.set(GuiIcons.ARROW_UP.getAsSprite(16, 16));
        addBtn.text.set(TextUtils.EMPTY);
        addBtn.componentRenderer.set(buttonsComponentRenderer.get());
        addBtn.addEventListener(DLGuiStandardEvents.MouseHoldDownEvent.class, (src, event) -> {
            changeValueOnScroll(1);
            return true;
        });
        addBtn.inputConsumptionPolicy.set((type) -> {
            return type != ConsumptionType.SCROLL;
        });
        addComponent(addBtn);

        subBtn = new DLButton(width() - BUTTON_WIDTH, height() / 2, BUTTON_WIDTH, height() / 2);
        subBtn.icon.set(GuiIcons.ARROW_DOWN.getAsSprite(16, 16));
        subBtn.text.set(TextUtils.EMPTY);
        subBtn.componentRenderer.set(buttonsComponentRenderer.get());
        subBtn.addEventListener(DLGuiStandardEvents.MouseHoldDownEvent.class, (src, event) -> {
            changeValueOnScroll(-1);
            return true;
        });
        subBtn.inputConsumptionPolicy.set((type) -> {
            return type != ConsumptionType.SCROLL;
        });
        addComponent(subBtn);
        
        updateButtons();
        this.showButtons.withAfterPropertyChangedCallback((o, val) -> {
            updateButtons();
        });        

        buttonsComponentRenderer.withAfterPropertyChangedCallback((o, val) -> {
            addBtn.componentRenderer.set(val);
            subBtn.componentRenderer.set(val);
        });

        DLContextMenu contextMenu = new DLContextMenu((pX, pY) -> {
            List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".menu.copy"), DLSprite.empty(), selectedItem.get().isPresent(), () -> {
                Minecraft.getInstance().keyboardHandler.setClipboard(selectedItem.get().map(i -> i.toString()).orElse(""));
            }, null));
            return entries;
        });

        addEventListener(DLGuiStandardEvents.RightClickEvent.class, (src, event) -> {
            contextMenu.open(getWindowManager(), (int)getWindowManager().mouseXOnScreen(), (int)getWindowManager().mouseYOnScreen());
            return false;
        });
    }
    
    public void changeValueOnScroll(int direction) {
        this.selectedIndex.set(MathUtils.clamp(this.selectedIndex.get() + direction * (DLWindowManager.hasShiftDown() ? shiftStep.get() : 1), 0, items.size() - 1));
    }

    protected void updateButtons() {
        addBtn.visible.set(showButtons.get());
        subBtn.visible.set(showButtons.get());
    }
    
    public boolean defaultButtonClickAction(DLGuiComponent src, ClickEvent event) {
        return false;
    }


    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        RenderSystem.enableBlend();
        GuiUtils.setTint(backgroundTint.get());

        if (!enabled.get()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ItemPickerState.DISABLED);
        } else if (isFocused()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ItemPickerState.FOCUSED);
        } else if (isSelected()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ItemPickerState.SELECTED);
        } else {            
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ItemPickerState.NORMAL);
        }

        GuiUtils.setTint(textColor.get());
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, 4, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, textFormat.get().combine(this), enabled.get() ? DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR : DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR, ETextAlignment.LEFT, drawFontShadow.get());
        GuiUtils.resetTint();
    }
    
}
