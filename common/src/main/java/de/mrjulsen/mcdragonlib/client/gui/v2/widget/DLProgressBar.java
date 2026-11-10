package de.mrjulsen.mcdragonlib.client.gui.v2.widget;


import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

public class DLProgressBar extends GuiComponent {

    public record ValueChangedEvent(double value) implements IEvent {}
    public record MaxValueChangedEvent(double max) implements IEvent {}
    public record BarColorChanged(DLColor color) implements IEvent {}
    public record StyleChanged(ProgressBarStyle style) implements IEvent {}

    public enum ProgressBarStyle {
        CONTINUOUS,
        INDETERMINATE
    }

    public static final Theme.Key<DLColor> THEME_KEY_BACKGROUND_COLOR = Theme.key(DragonLib.MODID + ".progressbar.background_color", DLColor.BLACK);
    public static final Theme.Key<DLColor> THEME_KEY_BORDER_COLOR = Theme.key(DragonLib.MODID + ".progressbar.border_color", DLColor.WHITE);
    public static final Theme.Key<Integer> THEME_KEY_BAR_GAP = Theme.key(DragonLib.MODID + ".progressbar.bar_gap", 1);
    public static final Theme.Key<Integer> THEME_KEY_BORDER_THICKNESS = Theme.key(DragonLib.MODID + ".progressbar.border_thickness", 1);
    public static final Theme.Key<Integer> THEME_KEY_INDETERMINATE_BAR_WIDTH = Theme.key(DragonLib.MODID + ".progressbar.indeterminate_bar_width", 20);

    public final NumberProperty<Integer> animationSpeed = new NumberProperty<>(2);
    public final NumberProperty<Double> max = new NumberProperty<>(1D)
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new MaxValueChangedEvent(val)));
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, () -> 0D, max::get)
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new ValueChangedEvent(val)));
    public final ColorProperty color = new ColorProperty(DLColor.GREEN, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new BarColorChanged(val)));
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

            int maxOffset = width() - getBarMargin() * 2;
            if (animationOffset > maxOffset) {
                animationOffset = -getThemeValue(THEME_KEY_INDETERMINATE_BAR_WIDTH);
            }
        }
    }

    protected final int getBarMargin() {
        return getThemeValue(THEME_KEY_BORDER_THICKNESS) + getThemeValue(THEME_KEY_BAR_GAP);
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.box(g, 0, 0, width(), height(), getThemeValue(THEME_KEY_BACKGROUND_COLOR), getThemeValue(THEME_KEY_BORDER_COLOR), getThemeValue(THEME_KEY_BORDER_THICKNESS));

        switch (style.get()) {
            case INDETERMINATE -> {
                int barAreaX = getBarMargin();
                int barAreaWidth = width() - getBarMargin() * 2;

                int rawX = barAreaX + animationOffset;
                int rawWidth = getThemeValue(THEME_KEY_INDETERMINATE_BAR_WIDTH);

                int visibleX = Math.max(rawX, barAreaX);
                int visibleEndX = Math.min(rawX + rawWidth, barAreaX + barAreaWidth);
                int visibleWidth = visibleEndX - visibleX;

                if (visibleWidth > 0) {
                    GuiUtils.fill(g, visibleX, getBarMargin(), visibleWidth, height() - getBarMargin() * 2, color.get());
                }
            }
            default -> {                
                int fillWidth = (int)((width() - 4) / max.get() * value.get());
                GuiUtils.fill(g, getBarMargin(), getBarMargin(), fillWidth, height() - getBarMargin() * 2, color.get());
            }
        }
    }
}

