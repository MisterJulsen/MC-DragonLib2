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
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * A clickable GUI button component used by the DragonLib GUI framework.
 *
 * <p>This component renders a background using an {@link IStateRenderer button renderer}, an optional icon,
 * and a text caption. It supports different visual states (normal, disabled, selected, down) and fires
 * standard GUI events. The class is annotated with supported event types for integration with the framework's
 * event system.</p>
 *
 * @see de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer
 * @see DLGuiStandardEvents
 */
@SupportsEvents({
    DLButton.CaptionChangedEvent.class,
    DLButton.BackgroundColorChangedEvent.class,
    DLButton.TextColorChangedEvent.class
})
public class DLButton extends DLGuiComponent {
    
    /**
     * Event fired when the button background tint color changes.
     *
     * @param color the new background color
     */
    public record BackgroundColorChangedEvent(DLColor color) implements IEvent {}

    /**
     * Event fired when the button text color changes.
     *
     * @param color the new text color
     */
    public record TextColorChangedEvent(DLColor color) implements IEvent {}

    /**
     * Event fired when the button caption text changes.
     *
     * @param text the new caption component (localized text)
     */
    public record CaptionChangedEvent(Component text) implements IEvent {}

    /**
     * Visual states that the button can be rendered in.
     *
     * <ul>
     *   <li>NORMAL - default state</li>
     *   <li>DISABLED - when the button is not enabled</li>
     *   <li>SELECTED - when the button is selected/highlighted</li>
     *   <li>DOWN - pressed state</li>
     *   <li>DOWN_SELECTED - pressed while selected</li>
     *   <li>DISABLED_SELECTED - disabled but selected</li>
     * </ul>
     */
    public static enum ButtonState {
        NORMAL,
        DISABLED,
        SELECTED,
        DOWN,
        DOWN_SELECTED,
        DISABLED_SELECTED;
    }

    /**
     * The event listener id for the default click action added during construction.
     *
     * <p>This id can be used to remove or reference the default click listener.</p>
     */
    public final EventListenerId defaultButtonClickEventId;

    /**
     * The displayed text (caption) as a {@link Component}.
     *
     * <p>Changing this property fires {@link DLButton.CaptionChangedEvent}.</p>
     */
    public final Property<Component> text = new Property<Component>(TextUtils.text(getClass().getSimpleName()))
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLButton.CaptionChangedEvent(a), true));

    /**
     * The color used to render the button text. This property is inheritable and may be overridden
     * by parent containers when {@code overrideLocal} is false.
     *
     * <p>Changing this property fires {@link DLButton.TextColorChangedEvent}.</p>
     */
    public final ColorProperty textColor = new ColorProperty(DLColor.UNDEFINED, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLButton.TextColorChangedEvent(a), true));

    /**
     * The tint color applied to the button background. This property is inheritable and may be overridden
     * by parent containers when {@code overrideLocal} is false.
     *
     * <p>Changing this property fires {@link DLButton.BackgroundColorChangedEvent}.</p>
     */
    public final ColorProperty backgroundTint = new ColorProperty(DLColor.UNDEFINED, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLButton.BackgroundColorChangedEvent(a), true));

    /**
     * Renderer used to draw the button background for different {@link ButtonState states}.
     *
     * <p>Default is {@link VanillaButtonRenderer#VANILLA_BUTTONS}.</p>
     */
    public final Property<IStateRenderer<ButtonState>> componentRenderer = new Property<>(VanillaButtonRenderer.VANILLA_BUTTONS);

    /**
     * Optional icon sprite rendered on the button.
     */
    public final Property<DLSprite> icon = new Property<>(DLSprite.empty());

    /**
     * Alignment for the caption text inside the button.
     */
    public final Property<ETextAlignment> textAlignment = new Property<>(ETextAlignment.CENTER);

    /**
     * Alignment for the icon inside the button.
     */
    public final Property<ETextAlignment> iconAlignment = new Property<>(ETextAlignment.CENTER);
    
    /**
     * Whether to draw a font shadow for the button caption. This property is inheritable.
     */
    public final BooleanProperty drawFontShadow = new BooleanProperty(true);


    /**
     * Creates a new button at the given x/y coordinates with a default size (100x20).
     *
     * @param x horizontal position of the button
     * @param y vertical position of the button
     */
    public DLButton(int x, int y) {
        this(x, y, 100, 20);
    }

    /**
     * Creates a new button at the given position with the specified width and height.
     *
     * @param x horizontal position of the button
     * @param y vertical position of the button
     * @param w button width in pixels
     * @param h button height in pixels
     */
    public DLButton(int x, int y, int w, int h) {
        super(x, y, w, h);
        defaultButtonClickEventId = addEventListener(DLGuiStandardEvents.ClickEvent.class, this::defaultButtonClickAction, -1000);  
    }

    /**
     * Default click action invoked when the button receives a click event.
     *
     * <p>The default implementation plays the vanilla UI button click sound. Returning {@code false}
     * allows other listeners to still process the event.</p>
     *
     * @param src the component that fired the event (typically this button)
     * @param event the click event details
     * @return {@code false} to indicate the event should not be consumed by this handler alone
     */
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

        if (hasIcon) {
            iconSprite.render(graphics, iconX + offset, iconY + offset);
        }

        GuiUtils.setTint(textColor.get());
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
