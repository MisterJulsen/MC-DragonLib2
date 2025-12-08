package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.Optional;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents.ClickEvent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.ITextFormatter;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty.ListOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@SupportsEvents({
    DLCycleButton.SelectedItemChanged.class
})
public class DLCycleButton<T> extends DLButton {

    public record SelectedItemChanged<T>(Optional<T> item, int index) implements IEvent {}
    
    public final ListProperty<T> items = new ListProperty<T>()
        .withUpdateCallback((val, operation) -> updateSelectedItem(operation == ListOperation.ADD));
    public final NumberProperty<Integer> selectedIndex = new NumberProperty<>(-1, -1, Integer.MAX_VALUE);
    public final Property<Optional<T>> selectedItem = new Property<>(Optional.empty());
    public final Property<ITextFormatter<DLCycleButton<T>>> textFormat = new Property<>((src) -> TextUtils.text(src.text.get().getString()).append(": ").append(src.selectedItem.get().map(x -> x.toString()).orElse("")).withStyle(src.text.get().getStyle()));
    public final BooleanProperty cycling = new BooleanProperty(true);
 
    public DLCycleButton(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.selectedIndex.withAfterPropertyChangedCallback((o, n) -> {
            if (!o.equals(n)) {
                Optional<T> item = Optional.ofNullable(n >= items.size() || n < 0 ? null : items.get(n));
                selectedItem.set(item);
                invokeEvent(this, new SelectedItemChanged<>(item, n));
            }
        });
        
        this.selectedItem.withAfterPropertyChangedCallback((o, n) -> {
            if (o.orElse(null) != n.orElse(null)) {
                selectedIndex.set(n.map(a -> items.indexOf(a)).orElse(-1));
            }
        });
    }

    @Override
    public boolean defaultButtonClickAction(DLGuiComponent src, ClickEvent event) {
        int targetIndex = selectedIndex.get();
        if (Screen.hasShiftDown()) {
            targetIndex--;
        } else {
            targetIndex++;
        }

        if (targetIndex < 0) {
            targetIndex = (cycling.get() ? items.size() - 1 : 0);
        } else if (targetIndex >= items.size()) {
            targetIndex = (cycling.get() ? 0 : items.size() - 1);
        }
        this.selectedIndex.set(targetIndex);

        return super.defaultButtonClickAction(src, event);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        RenderSystem.enableBlend();
        DLWindowManager manager = getWindowManager();
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
        Component buttonText = textFormat.get().combine(this);

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

    protected void updateSelectedItem(boolean firstIfUnselected) {
        if (selectedItem.get().isPresent() && this.items.contains(selectedItem.get().get())) {
            this.selectedIndex.set(this.items.indexOf(selectedItem.get().get()));
        } else {
            this.selectedIndex.set(firstIfUnselected ? 0 : -1);
        }
    }    
}
