package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.gui.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.client.gui.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.client.gui.properties.Property;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

@SupportsEvents({
    DLGuiCommonEvents.BackgroundColorChangedEvent.class,
    DLProgressBar.ValueChangedEvent.class,
    DLProgressBar.MaxValueChangedEvent.class,
    DLProgressBar.BarColorChanged.class,
    DLProgressBar.BorderColorChanged.class,
    DLProgressBar.StyleChanged.class,
})
public class DLProgressBar extends DLGuiComponent {
    
    public record ValueChangedEvent(double value) implements IEvent {}
    public record MaxValueChangedEvent(double max) implements IEvent {}
    public record BarColorChanged(DLColor color) implements IEvent {}
    public record BorderColorChanged(DLColor color) implements IEvent {}
    public record StyleChanged(ProgressBarStyle style) implements IEvent {}

    public static enum ProgressBarStyle {
        CONTINUOUS,
        INDETERMINATE
    }

    public final NumberProperty<Integer> indeterminateBarWidth = new NumberProperty<>(20);
    public final NumberProperty<Integer> animationSpeed = new NumberProperty<>(2);
    public final NumberProperty<Double> max = new NumberProperty<>(1D)
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new MaxValueChangedEvent(val)));
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, () -> 0D, () -> max.get())
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new ValueChangedEvent(val)));
    public final ColorProperty color = new ColorProperty(DLColor.GREEN, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new BarColorChanged(val)));
    public final ColorProperty backgroundColor = new ColorProperty(DLColor.BLACK, DLColor.BLACK)
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new DLGuiCommonEvents.BackgroundColorChangedEvent(val)));
    public final ColorProperty borderColor = new ColorProperty(DLColor.WHITE, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new BorderColorChanged(val)));
    public final Property<ProgressBarStyle> style = new Property<>(ProgressBarStyle.CONTINUOUS)
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new StyleChanged(val)));

    private int animationOffset = 0;

    public DLProgressBar(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public void tick() {
        super.tick();

        if (style.get() == ProgressBarStyle.INDETERMINATE) {
            animationOffset += animationSpeed.get();

            int maxOffset = width() - 4;
            if (animationOffset > maxOffset) {
                animationOffset = -indeterminateBarWidth.get();
            }
        }
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.fill(graphics, 0, 0, width(), height(), borderColor.get());
        GuiUtils.fill(graphics, 1, 1, width() - 2, height() - 2, backgroundColor.get());

        switch (style.get()) {
            case INDETERMINATE -> {
                int barAreaX = 2;
                int barAreaWidth = width() - 4;

                int rawX = barAreaX + animationOffset;
                int rawWidth = indeterminateBarWidth.get();

                int visibleX = Math.max(rawX, barAreaX);
                int visibleEndX = Math.min(rawX + rawWidth, barAreaX + barAreaWidth);
                int visibleWidth = visibleEndX - visibleX;

                if (visibleWidth > 0) {
                    GuiUtils.fill(graphics, visibleX, 2, visibleWidth, height() - 4, color.get());
                }
            }
            default -> {                
                int fillWidth = (int)((width() - 4) / max.get() * value.get());
                GuiUtils.fill(graphics, 2, 2, fillWidth, height() - 4, color.get());
            }
        }

        super.renderMainLayer(graphics, mouseX, mouseY, renderBounds);
    }
}

