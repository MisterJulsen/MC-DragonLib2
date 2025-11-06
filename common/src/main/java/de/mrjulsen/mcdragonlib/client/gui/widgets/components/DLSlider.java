package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton.ButtonState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.ITextFormatter;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.InheritableProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@SupportsEvents({
    DLSlider.CaptionChangedEvent.class,
    DLSlider.TextColorChangedEvent.class,
    DLSlider.BackgroundColorChangedEvent.class,
    DLSlider.ValueChangedEvent.class,
    DLSlider.ValueRangeChangedEvent.class,
    DLSlider.TextFormatChanged.class
})
public class DLSlider extends DLGuiComponent {
    
    public record BackgroundColorChangedEvent(DLColor color) implements IEvent {}
    public record TextColorChangedEvent(DLColor color) implements IEvent {}
    public record CaptionChangedEvent(Component text) implements IEvent {}
    public record ValueChangedEvent(double value) implements IEvent {}
    public record ValueRangeChangedEvent(double min, double max) implements IEvent {}
    public record TextFormatChanged<T extends DLGuiComponent>(ITextFormatter<T> format) implements IEvent {}

    public static final ITextFormatter<DLSlider> DEFAULT_TEXT_DOUBLE_VALUE_FORMAT = (src) -> TextUtils.text(src.text.get().getString()).append(": ").append(String.valueOf(src.value.get().doubleValue())).withStyle(src.text.get().getStyle());
    public static final ITextFormatter<DLSlider> DEFAULT_TEXT_INT_VALUE_FORMAT = (src) -> TextUtils.text(src.text.get().getString()).append(": ").append(String.valueOf(src.value.get().intValue())).withStyle(src.text.get().getStyle());
    public static final ITextFormatter<DLSlider> DEFAULT_TEXT_DOUBLE_PERCENTAGE_FORMAT = (src) -> TextUtils.text(src.text.get().getString()).append(": ").append(String.valueOf((int)(src.value.get().doubleValue() * 100D))).append("%").withStyle(src.text.get().getStyle());

    public final NumberProperty<Integer> sliderWidth = new NumberProperty<Integer>(8);
    public final NumberProperty<Double> step = new NumberProperty<Double>(1D);
    public final NumberProperty<Double> min = new NumberProperty<Double>(0D);
    public final NumberProperty<Double> max = new NumberProperty<Double>(100D);
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, () -> min.get(), () -> max.get())
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLSlider.ValueChangedEvent(a.doubleValue()), true));
    public final Property<ITextFormatter<DLSlider>> textFormat = new Property<ITextFormatter<DLSlider>>(DEFAULT_TEXT_INT_VALUE_FORMAT)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLSlider.TextFormatChanged<>(a), true));
    public final Property<Component> text = new Property<Component>(TextUtils.text(getClass().getSimpleName()))
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLSlider.CaptionChangedEvent(a), true));
    @InheritableProperty(overrideLocal = false)
    public final ColorProperty textColor = new ColorProperty(DLColor.UNDEFINED, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLSlider.TextColorChangedEvent(a), true));
    @InheritableProperty(overrideLocal = false)
    public final ColorProperty backgroundTint = new ColorProperty(DLColor.UNDEFINED, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLSlider.BackgroundColorChangedEvent(a), true));
    public final Property<IStateRenderer<ButtonState>> componentRenderer = new Property<>(VanillaButtonRenderer.VANILLA_BUTTONS);

    protected Component displayText = TextUtils.empty();

    public DLSlider(int x, int y, int w, int h) {
        super(x, y, w, h);
        this.min.withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLSlider.ValueRangeChangedEvent(a.doubleValue(), max.get()), true));
        this.max.withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLSlider.ValueRangeChangedEvent(min.get(), a.doubleValue()), true));
        addEventListener(DLGuiStandardEvents.DragEvent.class, (src, event) -> {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                updateSliderValue(event.mouseX(), event.mouseY());
            }
            return false;
        });
        addEventListener(DLGuiStandardEvents.MouseDownEvent.class, (src, event) -> {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                updateSliderValue(event.mouseX(), event.mouseY());
            }
            return true;
        });
        addEventListener(DLSlider.ValueRangeChangedEvent.class, (src, event) -> {
            clampValue();
            return false;
        });
        addEventListener(DLSlider.ValueChangedEvent.class, (src, event) -> {            
            displayText = textFormat.get().combine(this);
            return false;
        });
        addEventListener(DLSlider.TextFormatChanged.class, (src, event) -> {            
            displayText = textFormat.get().combine(this);
            return false;
        });

        clampValue();
        displayText = textFormat.get().combine(this);
    }

    protected void clampValue() {
        this.value.set(MathUtils.clamp(this.value.get(), min.get(), max.get()));
    }

    protected void updateSliderValue(double mouseX, double mouseY) {
        double value = (max.get() - min.get()) / (width() - sliderWidth.get()) * (mouseX - sliderWidth.get() / 2D);
        value = Math.round(value / step.get().doubleValue()) * step.get().doubleValue();
        this.value.set(value);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        super.renderMainLayer(graphics, mouseX, mouseY, renderBounds);
        GuiUtils.setTint(backgroundTint.get());

        componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DISABLED);
        int sliderX = (int)((double)(width() - sliderWidth.get()) / (max.get() - min.get()) * value.get());
        if (!enabled.get()) {
            componentRenderer.get().renderSprite(graphics, sliderX, 0, sliderWidth.get(), height(), this, ButtonState.DISABLED);
        } else if (isMouseDown() && getWindowManager().getMouseDownButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            componentRenderer.get().renderSprite(graphics, sliderX, 0, sliderWidth.get(), height(), this, ButtonState.DOWN_SELECTED);
        } else if (isSelected()) {
            componentRenderer.get().renderSprite(graphics, sliderX, 0, sliderWidth.get(), height(), this, ButtonState.SELECTED);
        } else {
            componentRenderer.get().renderSprite(graphics, sliderX, 0, sliderWidth.get(), height(), this, ButtonState.NORMAL);
        }

        GuiUtils.setTint(textColor.get());
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, width() / 2, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, displayText, enabled.get() ? DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR : DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR, ETextAlignment.CENTER, true);
        GuiUtils.resetTint();
    }
    
}
