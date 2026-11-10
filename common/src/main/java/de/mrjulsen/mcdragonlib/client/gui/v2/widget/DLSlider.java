package de.mrjulsen.mcdragonlib.client.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.IFormatter;
import de.mrjulsen.mcdragonlib.client.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class DLSlider extends GuiComponent {

    public static class Renderer implements WidgetRenderer<DLSlider> {

        public static final Renderer STANDARD = new Renderer(DLTextureSheet.VANILLA_BUTTON);

        public static final String SPRITE_NORMAL = "normal";
        public static final String SPRITE_PRESSED = "down_selected";
        public static final String SPRITE_HOVERED = "selected";
        public static final String SPRITE_DISABLED = "disabled";

        protected final DLTextureSheet spriteSheet;

        public Renderer(DLTextureSheet spriteSheet) {
            this.spriteSheet = spriteSheet;
        }

        @Override
        public void render(DLGuiGraphics g, DLSlider component, double x, double y, double width, double height) {
            GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_DISABLED), x, y, width, height);

            int sliderX;
            double range = component.max.get() - component.min.get();
            double trackWidth = Math.max(0D, width - component.sliderWidth.get());
            if (range <= 0D) {
                sliderX = 0;
            } else {
                sliderX = (int)(((component.value.get() - component.min.get()) / range ) * trackWidth);
            }
            if (!component.enabled.get()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_DISABLED), sliderX, y, component.sliderWidth.get(), height);
            } else if (component.isPressed()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_PRESSED), sliderX, y, component.sliderWidth.get(), height);
            } else if (component.isHovered()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_HOVERED), sliderX, y, component.sliderWidth.get(), height);
            } else {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_NORMAL), sliderX, y, component.sliderWidth.get(), height);
            }
        }
    }

    public record CaptionChangedEvent(Component text) implements IEvent {}
    public record ValueChangedEvent(double value) implements IEvent {}
    public record ValueRangeChangedEvent(double min, double max) implements IEvent {}
    public record TextFormatChanged<T extends GuiComponent>(IFormatter<T, Component> format) implements IEvent {}

    public static final Theme.Key<WidgetRenderer<DLSlider>> THEME_KEY_RENDERER = Theme.key(DragonLib.MODID + ".slider.renderer", Renderer.STANDARD);
    public static final Theme.Key<DLColor> THEME_KEY_BACKGROUND_COLOR = Theme.key(DragonLib.MODID + ".slider.background_color", DLColor.WHITE);

    protected static final int TEXT_PADDING = 6;
    protected static final int DEFAULT_BUTTON_HEIGHT = 20;
    protected static final int ICON_GAP = 4;

    public static final IFormatter<DLSlider, Component> TEXT_FORMAT_DOUBLE_VALUE = (src) -> TextUtils.text(src.text.get().getString()).append(": ").append(String.valueOf(src.value.get())).withStyle(src.text.get().getStyle());
    public static final IFormatter<DLSlider, Component> TEXT_FORMAT_INT_VALUE = (src) -> TextUtils.text(src.text.get().getString()).append(": ").append(String.valueOf(src.value.get().intValue())).withStyle(src.text.get().getStyle());
    public static final IFormatter<DLSlider, Component> TEXT_FORMAT_DOUBLE_PERCENTAGE = (src) -> TextUtils.text(src.text.get().getString()).append(": ").append(String.valueOf((int)(src.value.get() * 100D))).append("%").withStyle(src.text.get().getStyle());

    public final Property<Component> text = new Property<>(TextUtils.text(getClass().getSimpleName()));
    public final Property<IFormatter<DLSlider, Component>> textFormat = new Property<>(TEXT_FORMAT_INT_VALUE);
    public final Property<IFormatter<DLSlider, DLSprite>> icon = new Property<>((src) -> null);
    public final Property<ETextAlignment> textAlignment = new Property<>(ETextAlignment.CENTER);
    public final Property<ETextAlignment> iconAlignment = new Property<>(ETextAlignment.CENTER);
    public final BooleanProperty drawFontShadow = new BooleanProperty(true);
    public final NumberProperty<Double> step = new NumberProperty<>(1D);
    public final NumberProperty<Double> min = new NumberProperty<>(0D);
    public final NumberProperty<Double> max = new NumberProperty<>(10D);
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, min::get, max::get);
    public final NumberProperty<Integer> sliderWidth = new NumberProperty<>(8, 1, Integer.MAX_VALUE);

    protected Component displayText = TextUtils.empty();
    private boolean isDragging;

    public DLSlider(int x, int y, int width, int height) {
        super(x, y, width, height);
        focusable.set(true);
        clipsChildren.set(false);

        this.text.addChangeListener((o, n) -> {
            invalidateLayout();
            invokeEvent(this, new CaptionChangedEvent(n));
        });
        this.min.addChangeListener((o, n) -> invokeEvent(this, new ValueRangeChangedEvent(n, max.get()), true));
        this.max.addChangeListener((o, n) -> invokeEvent(this, new ValueRangeChangedEvent(min.get(), n), true));

        addEventListener(GuiEvents.DragEvent.class, (s, e) -> {
            if (e.leftClick()) {
                isDragging = true;
                updateSliderValue(e.localX(s), e.localY(s));
            }
        });
        addEventListener(GuiEvents.DragEndEvent.class, (s, e) -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            isDragging = false;
        });
        addEventListener(MouseDownEvent.class, (s, e) -> {
            if (e.leftClick()) {
                updateSliderValue(e.localX(s), e.localY(s));
            }
        });
        addEventListener(ValueRangeChangedEvent.class, (s, e) -> {
            clampValue();
        });
        addEventListener(ValueChangedEvent.class, (s, e) -> {
            displayText = textFormat.get().format(this);
        });
        addEventListener(TextFormatChanged.class, (s, e) -> {
            displayText = textFormat.get().format(this);
        });

        clampValue();
        displayText = textFormat.get().format(this);
    }

    @Override
    public CursorType getCurrentCursor() {
        if (isDragging) {
            return CursorType.HRESIZE;
        }
        return super.getCurrentCursor();
    }

    protected void clampValue() {
        this.value.set(MathUtils.clamp(this.value.get(), min.get(), max.get()));
    }

    protected void updateSliderValue(double mouseX, double mouseY) {
        double range = max.get() - min.get();
        double trackWidth = Math.max(0D, width() - sliderWidth.get());
        if (range <= 0D || trackWidth <= 0D) {
            this.value.set(min.get());
            return;
        }
        double relativeX = (mouseX - sliderWidth.get() / 2D) / trackWidth;
        relativeX = MathUtils.clamp(relativeX, 0D, 1D);
        double rawValue = min.get() + relativeX * range;
        rawValue = Math.round(rawValue / step.get()) * step.get();
        this.value.set(rawValue);
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.withTint(getThemeValue(THEME_KEY_BACKGROUND_COLOR), () -> {
            getThemeValue(THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight());
        });

        final DLSprite sprite = icon.get().format(this);
        final Component label = textFormat.get().format(this);
        final double iconWidth = sprite == null || sprite.isEmpty() ? 0 : sprite.getWidth();
        final double textWidth = label == null ? 0 : TextUtils.textWidth(g.font(), label);
        final double gap = iconWidth > 0 && textWidth > 0 ? ICON_GAP : 0;

        final double contentX = TEXT_PADDING;
        final double contentWidth = Math.max(0, dWidth() - TEXT_PADDING * 2);

        double iconX;
        double textAreaX;
        double textAreaWidth;

        switch (iconAlignment.get()) {
            case LEFT -> {
                iconX = contentX;
                textAreaX = contentX + iconWidth + gap;
                textAreaWidth = contentWidth - iconWidth - gap;
            }
            case RIGHT -> {
                iconX = contentX + contentWidth - iconWidth;
                textAreaX = contentX;
                textAreaWidth = contentWidth - iconWidth - gap;
            }
            default -> {
                ETextAlignment blockAlignment = textWidth > 0 ? textAlignment.get() : ETextAlignment.CENTER;
                iconX = contentX + Math.max(0, GuiUtils.alignmentOffset(blockAlignment, contentWidth, iconWidth + gap + textWidth));
                textAreaX = iconX + iconWidth + gap;
                textAreaWidth = textWidth;
            }
        }

        final double textX = textAreaX + Math.max(0, GuiUtils.alignmentOffset(textAlignment.get(), textAreaWidth, textWidth));
        if (iconWidth > 0) {
            sprite.render(g.legacy(), (int)iconX, (int)((dHeight() - sprite.getHeight()) / 2.0D));
        }
        if (textWidth > 0) {
            GuiUtils.text(g, label, textX, TextUtils.centerTextY(g.font(), dHeight()), getThemeValue(!enabled.get() ? Theme.TEXT_DISABLED : Theme.TEXT), drawFontShadow.get());
        }
    }
}
