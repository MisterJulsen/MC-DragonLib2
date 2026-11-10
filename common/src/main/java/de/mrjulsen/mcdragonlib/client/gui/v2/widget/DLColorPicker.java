package de.mrjulsen.mcdragonlib.client.gui.v2.widget;

import com.mojang.blaze3d.platform.NativeImage;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.GradientDirection;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLColor.ColorChannel;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.util.function.Consumer;

public class DLColorPicker extends GuiComponent {

    public static class Renderer implements WidgetRenderer<DLColorPicker> {

        public static final Renderer STANDARD = new Renderer(DLTextureSheet.DRAGONLIB_UI);

        public static final String SPRITE_FRAME = "slot";
        public static final String SPRITE_TRANSPARENCY = "transparency";

        protected final DLTextureSheet spriteSheet;

        public Renderer(DLTextureSheet spriteSheet) {
            this.spriteSheet = spriteSheet;
        }

        @Override
        public void render(DLGuiGraphics g, DLColorPicker component, double x, double y, double width, double height) {
            DLColor color = component.color.get();
            boolean vertical = component.sliderPosition.get().isVertical();

            Rectangle quad = component.quadArea();
            renderFrame(g, quad);
            GuiUtils.texture(g, component.quadTexture(), quad.x(), quad.y(), quad.width(), quad.height());

            Rectangle slider = component.sliderArea();
            if (slider != null) {
                renderFrame(g, slider);
                GuiUtils.texture(g, component.sliderTexture(), slider.x(), slider.y(), slider.width(), slider.height());
            }

            Rectangle alpha = component.alphaArea();
            if (alpha != null) {
                renderFrame(g, alpha);
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_TRANSPARENCY), alpha.x(), alpha.y(), alpha.width(), alpha.height());
                GuiUtils.fillGradient(g, alpha, DLColor.TRANSPARENT, color.withAlpha(255), vertical ? GradientDirection.TOP_TO_BOTTOM : GradientDirection.LEFT_TO_RIGHT);
            }

            renderQuadCursor(g, component, component.quadCursorX(), component.quadCursorY(), color);
            if (slider != null) {
                renderSliderCursor(g, slider, vertical, component.sliderCursor(), component.hsvSlots.get().slider().previewColor(component, component.hsvSlots.get().slider().get(component)));
            }
            if (alpha != null) {
                renderSliderCursor(g, alpha, vertical, component.alphaCursor(), color);
            }
        }

        protected void renderFrame(DLGuiGraphics g, Rectangle area) {
            GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_FRAME), area.x() - 1, area.y() - 1, area.width() + 2, area.height() + 2);
        }

        protected void renderQuadCursor(DLGuiGraphics g, DLColorPicker component, double cursorX, double cursorY, DLColor color) {
            GuiUtils.box(g, cursorX - 3, cursorY - 3, 6, 6, color, DLColor.pickBasedOnBrightness(color, DLColor.WHITE, DLColor.BLACK, 0.5f));
        }

        protected void renderSliderCursor(DLGuiGraphics g, Rectangle area, boolean vertical, double cursor, DLColor fill) {
            if (vertical) {
                GuiUtils.box(g, area.left() - 3, cursor - 3, area.width() + 6, 6, fill, DLColor.BLACK);
            } else {
                GuiUtils.box(g, cursor - 3, area.top() - 3, 6, area.height() + 6, fill, DLColor.BLACK);
            }
        }
    }

    public record ColorChangedEvent(DLColor color) implements IEvent {}

    public enum SliderPosition {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM;

        public boolean isVertical() {
            return this == LEFT || this == RIGHT;
        }
    }

    public enum HSVSlot {
        H,
        S,
        V;

        public float get(DLColorPicker picker) {
            return switch (this) {
                case H -> picker.hue;
                case S -> picker.saturation;
                case V -> picker.brightness;
            };
        }

        void set(DLColorPicker picker, float value) {
            switch (this) {
                case H -> picker.hue = value;
                case S -> picker.saturation = value;
                case V -> picker.brightness = value;
            }
        }

        public DLColor previewColor(DLColorPicker picker, float value) {
            return switch (this) {
                case H -> DLColor.fromHsv(value * 360, 1f, 1f);
                case S -> DLColor.fromHsv(picker.hue * 360, value, 1f);
                case V -> DLColor.fromHsv(picker.hue * 360, 0f, value);
            };
        }
    }

    public record HSVSlots(HSVSlot quadX, HSVSlot quadY, HSVSlot slider) {
        public HSVSlots {
            if (quadX == quadY || quadX == slider || quadY == slider) {
                throw new IllegalArgumentException("Every HSV channel must be used exactly once: " + quadX + ", " + quadY + ", " + slider);
            }
        }

        public DLColor colorOf(float x, float y, float s) {
            float[] hsv = {1, 1, 1};
            hsv[quadX.ordinal()] = x;
            hsv[quadY.ordinal()] = y;
            hsv[slider.ordinal()] = s;
            return DLColor.fromHsv(hsv[0] * 360, hsv[1], hsv[2]);
        }
    }

    private enum Region {
        QUAD,
        SLIDER,
        ALPHA
    }

    public static final Theme.Key<WidgetRenderer<DLColorPicker>> THEME_KEY_RENDERER = Theme.key(DragonLib.MODID + ".colorpicker.renderer", Renderer.STANDARD);
    public static final Theme.Key<Integer> THEME_KEY_SLIDER_SIZE = Theme.key(DragonLib.MODID + ".colorpicker.slider_size", 10);
    public static final Theme.Key<Integer> THEME_KEY_SPACING = Theme.key(DragonLib.MODID + ".colorpicker.spacing", 4);

    public final ColorProperty color = new ColorProperty(DLColor.WHITE, DLColor.WHITE);
    public final Property<HSVSlots> hsvSlots = new Property<>(new HSVSlots(HSVSlot.S, HSVSlot.V, HSVSlot.H));
    public final Property<SliderPosition> sliderPosition = new Property<>(SliderPosition.RIGHT);
    public final BooleanProperty showColorSlider = new BooleanProperty(true);
    public final BooleanProperty showAlphaSlider = new BooleanProperty(false);

    private float hue = 0f;
    private float saturation = 0f;
    private float brightness = 1f;
    private float alpha = 1f;

    private Rectangle quadArea;
    private Rectangle sliderArea;
    private Rectangle alphaArea;
    private boolean areasDirty = true;

    private DynamicTexture quadTexture;
    private DynamicTexture sliderTexture;
    private boolean quadTextureDirty = true;
    private boolean sliderTextureDirty = true;

    private Region activeRegion;
    private Region hoveredRegion;
    private boolean updatingColor;

    public DLColorPicker(int x, int y, int width, int height) {
        super(x, y, width, height);
        clipsContent.set(false);

        color.addChangeListener((o, n) -> {
            if (!updatingColor) {
                syncFromColor(n);
            }
            invokeEvent(this, new ColorChangedEvent(n), false);
        });
        hsvSlots.addChangeListener((o, n) -> invalidateTextures());
        sliderPosition.addChangeListener((o, n) -> {
            invalidateAreas();
            invalidateTextures();
        });
        showColorSlider.addChangeListener((o, n) -> invalidateAreas());
        showAlphaSlider.addChangeListener((o, n) -> invalidateAreas());

        addEventListener(GuiEvents.BoundsChangedEvent.class, (s, e) -> invalidateAreas());
        addEventListener(GuiEvents.LayoutUpdatedEvent.class, (s, e) -> invalidateAreas());
        addEventListener(GuiEvents.MouseDownEvent.class, (s, e) -> {
            if (e.leftClick()) {
                activeRegion = regionAt(e.localX(s), e.localY(s));
                pick(e.localX(s), e.localY(s));
            }
        });
        addEventListener(GuiEvents.DragEvent.class, (s, e) -> {
            if (e.leftClick()) {
                pick(e.localX(s), e.localY(s));
            }
        });
        addEventListener(GuiEvents.DragEndEvent.class, (s, e) -> activeRegion = null);
        addEventListener(GuiEvents.MouseReleaseEvent.class, (s, e) -> activeRegion = null);
        addEventListener(GuiEvents.MouseMoveEvent.class, (s, e) -> hoveredRegion = regionAt(e.localX(s), e.localY(s)));
        addEventListener(GuiEvents.MouseLeaveEvent.class, (s, e) -> hoveredRegion = null);

        Tooltip.attach(this, () -> (activeRegion == null ? hoveredRegion : activeRegion) == Region.ALPHA
            ? Tooltip.of(TextUtils.translate("gui." + DragonLib.MODID + ".colorpicker.opacity").append(" " + Math.round(alpha * 100) + "%"))
            : Tooltip.EMPTY
        );

        syncFromColor(color.get());
    }

    private void syncFromColor(DLColor c) {
        float s = c.getSaturation();
        float v = c.getBrightness();
        if (v > 0) {
            if (s > 0) {
                hue = c.getHue() / 360f;
            }
            saturation = s;
        }
        brightness = v;
        alpha = c.getAlphaF();
        invalidateTextures();
    }

    private void pick(double localX, double localY) {
        if (activeRegion == null) {
            return;
        }
        HSVSlots slots = hsvSlots.get();
        boolean vertical = sliderPosition.get().isVertical();
        switch (activeRegion) {
            case QUAD -> {
                Rectangle area = quadArea();
                slots.quadX().set(this, relative(localX, area.x(), area.width()));
                slots.quadY().set(this, 1f - relative(localY, area.y(), area.height()));
                sliderTextureDirty = true;
            }
            case SLIDER -> {
                Rectangle area = sliderArea();
                float rel = vertical ? relative(localY, area.y(), area.height()) : relative(localX, area.x(), area.width());
                slots.slider().set(this, 1f - rel);
                quadTextureDirty = true;
            }
            case ALPHA -> {
                Rectangle area = alphaArea();
                alpha = vertical ? relative(localY, area.y(), area.height()) : relative(localX, area.x(), area.width());
            }
        }
        updatingColor = true;
        try {
            color.set(DLColor.fromHsv(hue * 360, saturation, brightness).withAlpha(Math.round(alpha * 255)));
        } finally {
            updatingColor = false;
        }
    }

    private static float relative(double pos, double start, double length) {
        return length <= 0 ? 0f : MathUtils.clamp((float)((pos - start) / length), 0f, 1f);
    }

    private Region regionAt(double localX, double localY) {
        if (quadArea().collision(localX, localY)) return Region.QUAD;
        if (sliderArea() != null && sliderArea().collision(localX, localY)) return Region.SLIDER;
        if (alphaArea() != null && alphaArea().collision(localX, localY)) return Region.ALPHA;
        return null;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getAlpha() {
        return alpha;
    }

    private void invalidateAreas() {
        areasDirty = true;
    }

    private void invalidateTextures() {
        quadTextureDirty = true;
        sliderTextureDirty = true;
    }

    private void updateAreas() {
        if (!areasDirty) {
            return;
        }
        areasDirty = false;

        int sliderSize = getThemeValue(THEME_KEY_SLIDER_SIZE);
        int spacing = getThemeValue(THEME_KEY_SPACING);
        SliderPosition pos = sliderPosition.get();
        boolean vertical = pos.isVertical();
        int w = width();
        int h = height();

        int count = (showColorSlider.get() ? 1 : 0) + (showAlphaSlider.get() ? 1 : 0);
        int strip = count * (sliderSize + spacing);
        int quadLength = Math.max(0, (vertical ? w : h) - strip);
        int quadStart = (pos == SliderPosition.LEFT || pos == SliderPosition.TOP) ? strip : 0;

        quadArea = vertical ? inner(quadStart, 0, quadLength, h) : inner(0, quadStart, w, quadLength);

        Rectangle[] sliders = new Rectangle[count];
        for (int i = 0; i < count; i++) {
            int offset = (pos == SliderPosition.LEFT || pos == SliderPosition.TOP) ? strip - (i + 1) * (sliderSize + spacing) : quadLength + spacing + i * (sliderSize + spacing);
            sliders[i] = vertical ? inner(offset, 0, sliderSize, h) : inner(0, offset, w, sliderSize);
        }
        int next = 0;
        sliderArea = showColorSlider.get() ? sliders[next++] : null;
        alphaArea = showAlphaSlider.get() ? sliders[next] : null;
    }

    private static Rectangle inner(int x, int y, int w, int h) {
        return Rectangle.withSize(x + 1, y + 1, Math.max(0, w - 2), Math.max(0, h - 2));
    }

    public Rectangle quadArea() {
        updateAreas();
        return quadArea;
    }

    public Rectangle sliderArea() {
        updateAreas();
        return sliderArea;
    }

    public Rectangle alphaArea() {
        updateAreas();
        return alphaArea;
    }

    public double quadCursorX() {
        Rectangle area = quadArea();
        return area.x() + hsvSlots.get().quadX().get(this) * area.width();
    }

    public double quadCursorY() {
        Rectangle area = quadArea();
        return area.y() + (1f - hsvSlots.get().quadY().get(this)) * area.height();
    }

    public double sliderCursor() {
        Rectangle area = sliderArea();
        float value = 1f - hsvSlots.get().slider().get(this);
        return sliderPosition.get().isVertical() ? area.y() + value * area.height() : area.x() + value * area.width();
    }

    public double alphaCursor() {
        Rectangle area = alphaArea();
        return sliderPosition.get().isVertical() ? area.y() + alpha * area.height() : area.x() + alpha * area.width();
    }

    public DLTexture quadTexture() {
        Rectangle area = quadArea();
        HSVSlots slots = hsvSlots.get();
        float sliderValue = slots.slider().get(this);
        quadTexture = prepareTexture(quadTexture, area, quadTextureDirty, img -> {
            int w = img.getWidth();
            int h = img.getHeight();
            for (int px = 0; px < w; px++) {
                for (int py = 0; py < h; py++) {
                    img.setPixelRGBA(px, py, toAbgr(slots.colorOf((float)px / w, 1f - (float)py / h, sliderValue)));
                }
            }
        });
        quadTextureDirty = false;
        return asTexture(quadTexture);
    }

    public DLTexture sliderTexture() {
        Rectangle area = sliderArea();
        if (area == null) {
            return null;
        }
        HSVSlot slot = hsvSlots.get().slider();
        boolean vertical = sliderPosition.get().isVertical();
        sliderTexture = prepareTexture(sliderTexture, area, sliderTextureDirty, img -> {
            int w = img.getWidth();
            int h = img.getHeight();
            int length = vertical ? h : w;
            for (int i = 0; i < length; i++) {
                int abgr = toAbgr(slot.previewColor(this, 1f - (float)i / length));
                for (int k = 0; k < (vertical ? w : h); k++) {
                    if (vertical) {
                        img.setPixelRGBA(k, i, abgr);
                    } else {
                        img.setPixelRGBA(i, k, abgr);
                    }
                }
            }
        });
        sliderTextureDirty = false;
        return asTexture(sliderTexture);
    }

    private static DynamicTexture prepareTexture(DynamicTexture texture, Rectangle area, boolean dirty, Consumer<NativeImage> painter) {
        int w = Math.max(1, (int)area.width());
        int h = Math.max(1, (int)area.height());
        if (texture != null && (texture.getPixels() == null || texture.getPixels().getWidth() != w || texture.getPixels().getHeight() != h)) {
            texture.close();
            texture = null;
        }
        if (texture == null) {
            texture = new DynamicTexture(w, h, false);
            dirty = true;
        }
        if (dirty) {
            painter.accept(texture.getPixels());
            texture.upload();
        }
        return texture;
    }

    private static DLTexture asTexture(DynamicTexture texture) {
        NativeImage img = texture.getPixels();
        return new DLTexture(texture.getId(), img.getWidth(), img.getHeight());
    }

    private static int toAbgr(DLColor color) {
        return color.swapChannels(ColorChannel.R, ColorChannel.B).getAsARGB();
    }

    private void releaseTextures() {
        if (quadTexture != null) {
            quadTexture.close();
            quadTexture = null;
        }
        if (sliderTexture != null) {
            sliderTexture.close();
            sliderTexture = null;
        }
        invalidateTextures();
    }

    @Override
    public CursorType getCurrentCursor() {
        if (isEnabled() && (activeRegion != null || hoveredRegion != null)) {
            return CursorType.CROSSHAIR;
        }
        return super.getCurrentCursor();
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        getThemeValue(THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight());
    }

    @Override
    public void dispose() {
        super.dispose();
        releaseTextures();
    }
}
