package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaButtonRenderer;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.InheritableProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

@SupportsEvents({
    DLButton.CaptionChangedEvent.class,
    DLButton.BackgroundColorChangedEvent.class,
    DLButton.TextColorChangedEvent.class
})
public class DLButton extends DLGuiComponent {
    
    public record BackgroundColorChangedEvent(DLColor color) implements IEvent {}
    public record TextColorChangedEvent(DLColor color) implements IEvent {}
    public record CaptionChangedEvent(Component text) implements IEvent {}

    public static enum ButtonState {
        NORMAL,
        DISABLED,
        SELECTED,
        DOWN,
        DOWN_SELECTED,
        DISABLED_SELECTED;
    }

    public final EventListenerId defaultButtonClickEventId;

    public final Property<Component> text = new Property<Component>(TextUtils.text(getClass().getSimpleName()))
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLButton.CaptionChangedEvent(a), true));
    @InheritableProperty(overrideLocal = false)
    public final ColorProperty textColor = new ColorProperty(DLColor.UNDEFINED, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLButton.TextColorChangedEvent(a), true));
    @InheritableProperty(overrideLocal = false)
    public final ColorProperty backgroundTint = new ColorProperty(DLColor.UNDEFINED, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLButton.BackgroundColorChangedEvent(a), true));
    public final Property<IStateRenderer<ButtonState>> componentRenderer = new Property<>(VanillaButtonRenderer.VANILLA_BUTTONS);
    public final Property<DLSprite> icon = new Property<>(DLSprite.empty());
    public final Property<ETextAlignment> textAlignment = new Property<>(ETextAlignment.CENTER);
    public final Property<ETextAlignment> iconAlignment = new Property<>(ETextAlignment.CENTER);
    
    @InheritableProperty(overrideLocal = false)
    public final BooleanProperty drawFontShadow = new BooleanProperty(true, true);


    public DLButton(int x, int y) {
        this(x, y, 100, 20);
    }

    public DLButton(int x, int y, int w, int h) {
        super(x, y, w, h);
        defaultButtonClickEventId = addEventListener(DLGuiStandardEvents.ClickEvent.class, this::defaultButtonClickAction, -1000);  
    }

    public boolean defaultButtonClickAction(DLGuiComponent src, DLGuiStandardEvents.ClickEvent event) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return false;
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        super.renderMainLayer(graphics, mouseX, mouseY, renderBounds);

        RenderSystem.enableBlend();
        DLWindowManager manager = getWindowManager();

        // Hintergrund rendern
        GuiUtils.setTint(backgroundTint.get());
        if (!enabled.get()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DISABLED);
        } else if (isMouseDown() && (manager != null ? manager.getMouseDownButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT : true)) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DOWN_SELECTED);
        } else if (isSelected()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.SELECTED);
        } else {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.NORMAL);
        }

        DLSprite iconSprite = icon.get();
        boolean hasIcon = iconSprite != null && !iconSprite.isEmpty();
        Component buttonText = text.get();

        int textWidth = Minecraft.getInstance().font.width(buttonText);
        int textHeight = Minecraft.getInstance().font.lineHeight;
        int iconWidth = hasIcon ? iconSprite.getWidth() : 0;
        int iconHeight = hasIcon ? iconSprite.getHeight() : 0;
        int spacing = hasIcon && !buttonText.getString().isEmpty() ? 4 : 0;

        int centerY = height() / 2;
        int offset = isMouseDown() ? 1 : 0;
        int iconY = centerY - iconHeight / 2;
        int textY = centerY - textHeight / 2;
        int iconX = 0;
        int textX = 0;

        int buttonWidth = width();

        switch (iconAlignment.get()) {
            case LEFT -> {
                iconX = 4;
                int textStartX = iconX + iconWidth + spacing;
                switch (textAlignment.get()) {
                    case LEFT -> textX = Math.max(textStartX, 4);
                    case CENTER -> {
                        int centerTextX = buttonWidth / 2 - textWidth / 2;
                        textX = Math.max(centerTextX, textStartX);
                    }
                    case RIGHT -> textX = Math.max(buttonWidth - textWidth - 4, textStartX);
                }
            }
            case RIGHT -> {
                iconX = buttonWidth - iconWidth - 4;
                int maxTextRight = iconX - spacing;
                switch (textAlignment.get()) {
                    case LEFT -> textX = 4;
                    case CENTER -> {
                        int centerTextX = buttonWidth / 2 - textWidth / 2;
                        textX = Math.min(centerTextX, maxTextRight - textWidth);
                    }
                    case RIGHT -> textX = Math.min(buttonWidth - textWidth - 4, maxTextRight - textWidth);
                }
            }
            case CENTER -> {
                if (textAlignment.get() == ETextAlignment.CENTER) {
                    int totalWidth = iconWidth + spacing + textWidth;
                    int startX = (buttonWidth - totalWidth) / 2;

                    iconX = startX;
                    textX = iconX + iconWidth + spacing;
                } else {
                    switch (textAlignment.get()) {
                        case LEFT -> textX = 4;
                        case CENTER -> textX = buttonWidth / 2 - textWidth / 2;
                        case RIGHT -> textX = buttonWidth - textWidth - 4;
                    }

                    int centerIconX = buttonWidth / 2 - iconWidth / 2;
                    if (textX < centerIconX + iconWidth && textX + textWidth > centerIconX) {
                        iconX = textX + textWidth + spacing;
                        if (iconX + iconWidth > buttonWidth - 4) {
                            iconX = textX - iconWidth - spacing;
                            if (iconX < 4) {
                                iconX = centerIconX;
                            }
                        }
                    } else {
                        iconX = centerIconX;
                    }
                }
            }
            default -> {
                iconX = 4;
                int textStartX = iconX + iconWidth + spacing;
                textX = Math.max(textStartX, 4);
            }
        }

        GuiUtils.setTint(textColor.get());

        if (hasIcon) {
            iconSprite.render(graphics, iconX + offset, iconY + offset);
        }

        GuiUtils.drawString(
            graphics,
            Minecraft.getInstance().font,
            textX + offset,
            textY + offset,
            buttonText,
            enabled.get() ? DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR : DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR,
            ETextAlignment.LEFT,
            drawFontShadow.get()
        );

        GuiUtils.resetTint();
    }



}
