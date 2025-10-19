package de.mrjulsen.mcdragonlib.client.newgui.widgets.components;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiStandardEvents.ClickEvent;
import de.mrjulsen.mcdragonlib.client.newgui.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

@SupportsEvents({
    DLGuiCommonEvents.CheckedChangedEvent.class
})
public class DLToggleButton extends DLButton {

    public final BooleanProperty checked = new BooleanProperty(false, false)
        .withAfterPropertyChangedCallback((o, x) -> invokeEvent(this, new DLGuiCommonEvents.CheckedChangedEvent(x), true));
    public final BooleanProperty radioButtonMode = new BooleanProperty(false, false);

    public DLToggleButton(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public boolean defaultButtonClickAction(DLGuiComponent src, ClickEvent event) {
        if (radioButtonMode.get()) checked.set(true);
        else checked.toggle();

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, checked.get() ? 0.75f : 1.0F));
        if (radioButtonMode.get() && checked.get()) {
            getParent().ifPresent(parent -> {
                parent.forEachComponentMatching(
                    getClass(),
                    x -> x != this && x.radioButtonMode.get(),
                    c -> c.checked.set(false)
                );
            });
        }
        return false;
    }

    @Override
    public void renderMainLayer(Graphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.setTint(backgroundTint.get());
        DLWindowManager manager = getWindowManager();
        if (checked.get()) {
            if (!enabled.get()) {
                componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DISABLED);
            } else if (isMouseDown() && manager.getMouseDownButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DOWN_SELECTED);
            } else if (isSelected()) {
                componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DOWN_SELECTED);
            } else {
                componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DOWN);
            }
        } else {
            if (!enabled.get()) {
                componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DISABLED);
            } else if (isMouseDown() && manager.getMouseDownButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DOWN_SELECTED);
            } else if (isSelected()) {
                componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.SELECTED);
            } else {
                componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.NORMAL);
            }
        }
        GuiUtils.setTint(textColor.get());
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, width() / 2 + ((isMouseDown() || checked.get()) ? 1 : 0), height() / 2 + ((isMouseDown() || checked.get()) ? 1 : 0) - Minecraft.getInstance().font.lineHeight / 2, text.get(), enabled.get() ? DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, ETextAlignment.CENTER, true);
        GuiUtils.resetTint();
    }
    
}
