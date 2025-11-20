package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.mojang.blaze3d.platform.NativeImage;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.Cache;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.Pair;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.DLColor.ColorChannel;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.renderer.texture.DynamicTexture;

@SupportsEvents({
    DLColorPicker.ColorChangedEvent.class
})
public class DLColorPicker extends DLGuiComponent {
    
    public static final int SLIDER_SIZE = 10;
    public static final int INNER_PADDING = 4;
    
    public record ColorChangedEvent(DLColor color) implements IEvent {}

    private static enum HSVSlot {
        H((instance) -> instance.selectedHue, (instance, val) -> instance.selectedHue = val),
        S((instance) -> instance.selectedSaturation, (instance, val) -> instance.selectedSaturation = val),
        V((instance) -> instance.selectedBrightness, (instance, val) -> instance.selectedBrightness = val);

        private final Function<DLColorPicker, Float> getter;
        private final BiConsumer<DLColorPicker, Float> setter;

        private HSVSlot(Function<DLColorPicker, Float> getter, BiConsumer<DLColorPicker, Float> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public float get(DLColorPicker instance) {
            return getter.apply(instance);
        }

        public void set(DLColorPicker instance, float f) {
            setter.accept(instance, f);
        }

        public DLColor colorOf(DLColorPicker instance) {
            return colorOf(instance, this, get(instance));
        }

        public static DLColor colorOf(DLColorPicker instance, HSVSlot channel, float f) {
            return switch (channel) {
                case H -> DLColor.fromHsv(f * 360, 1f, 1f);
                case S -> DLColor.fromHsv(instance.selectedHue * 360, f, 1f);
                case V -> DLColor.fromHsv(instance.selectedHue * 360, 0f, f);
            };
        }
        
        public static DLColor colorOf(Pair<HSVSlot, Float> a, Pair<HSVSlot, Float> b, Pair<HSVSlot, Float> c) {
            float h = 360;
            float s = 1;
            float v = 1;

            switch (a.getFirst()) {
                case H -> h = a.getSecond() * 360;
                case S -> s = a.getSecond();
                case V -> v = a.getSecond();
            }
            switch (b.getFirst()) {
                case H -> h = b.getSecond() * 360;
                case S -> s = b.getSecond();
                case V -> v = b.getSecond();
            }
            switch (c.getFirst()) {
                case H -> h = c.getSecond() * 360;
                case S -> s = c.getSecond();
                case V -> v = c.getSecond();
            }

            return DLColor.fromHsv(h, s, v);
        }
    }

    public static record HSVSlots(HSVSlot quadX, HSVSlot quadY, HSVSlot slider) {}

    public final ColorProperty color = new ColorProperty(DLColor.WHITE, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new ColorChangedEvent(a), false));
    public final Property<HSVSlots> hsvSlots = new Property<>(new HSVSlots(HSVSlot.S, HSVSlot.V, HSVSlot.H));
    public final Property<EAlign> sliderAlign = new Property<>(EAlign.RIGHT)
        .withAfterPropertyChangedCallback((o, a) -> updateScreenLayout());
    public final BooleanProperty showColorSlider = new BooleanProperty(true, false)
        .withAfterPropertyChangedCallback((o, a) -> updateScreenLayout());
    public final BooleanProperty showAlphaSlider = new BooleanProperty(false, false)
        .withAfterPropertyChangedCallback((o, a) -> updateScreenLayout());
    
    
    private float selectedHue = 0f;
    private float selectedSaturation = 0f;
    private float selectedBrightness = 1f;
    private float selectedAlpha = 1f;

    private final Cache<Rectangle> quadArea;
    private final Cache<Rectangle> colorSliderArea;
    private final Cache<Rectangle> alphaSliderArea;    
    private final Cache<DynamicTexture> quadTexture;
    private final Cache<DynamicTexture> colorSliderTexture;

    private boolean skipValueUpdate = false;

    public DLColorPicker(int x, int y, int w, int h) {
        super(x, y, w, h);
        
        this.quadArea = new Cache<>(() -> {
            int sliderSize = 0;
            if (showColorSlider.get()) {
                sliderSize += INNER_PADDING + SLIDER_SIZE;
            }
            if (showAlphaSlider.get()) {
                sliderSize += INNER_PADDING + SLIDER_SIZE;
            }
            return Rectangle.withSize(
                1 + (sliderAlign.get() == EAlign.LEFT ? sliderSize : 0),
                1 + (sliderAlign.get() == EAlign.TOP ? sliderSize : 0),
                width() - 2 - (sliderAlign.get().isHorizontal() ? sliderSize : 0),
                height() - 2 - (sliderAlign.get().isVertical() ? sliderSize : 0)
            );
        });
        
        this.colorSliderArea = new Cache<>(() -> {
            int sliderSize = 0;
            if (showColorSlider.get()) {
                sliderSize += INNER_PADDING + SLIDER_SIZE;
            }
            if (showAlphaSlider.get()) {
                sliderSize += INNER_PADDING + SLIDER_SIZE;
            }
            return Rectangle.withSize(
                1 + (sliderAlign.get() == EAlign.RIGHT ? width() - sliderSize + INNER_PADDING : 0),
                1 + (sliderAlign.get() == EAlign.BOTTOM ? height() - sliderSize + INNER_PADDING : 0),
                (sliderAlign.get().isVertical() ? width() : SLIDER_SIZE) - 2,
                (sliderAlign.get().isHorizontal() ? height() : SLIDER_SIZE) - 2
            );
        });        
        
        this.alphaSliderArea = new Cache<>(() -> {
            return Rectangle.withSize(
                1 + (sliderAlign.get() == EAlign.RIGHT ? width() - SLIDER_SIZE : (sliderAlign.get().isHorizontal() ? SLIDER_SIZE + INNER_PADDING : 0)),
                1 + (sliderAlign.get() == EAlign.BOTTOM ? height() - SLIDER_SIZE : (sliderAlign.get().isVertical() ? SLIDER_SIZE + INNER_PADDING : 0)),
                (sliderAlign.get().isVertical() ? width() : SLIDER_SIZE) - 2,
                (sliderAlign.get().isHorizontal() ? height() : SLIDER_SIZE) - 2
            );
        });

        this.quadTexture = new Cache<>(() -> {
            NativeImage img = new NativeImage((int)quadArea.get().width(), (int)quadArea.get().height(), false);
            for (int i = 0; i < (int)quadArea.get().width(); i++) {
                for (int k = 0; k < (int)quadArea.get().height(); k++) {                
                    float vX = ((float)i / (int)quadArea.get().width());
                    float vY = 1f - ((float)k / (int)quadArea.get().height());
                    DLColor color = HSVSlot.colorOf(Pair.of(hsvSlots.get().quadX(), vX), Pair.of(hsvSlots.get().quadY(), vY), Pair.of(hsvSlots.get().slider(), hsvSlots.get().slider().get(this)));
                    color = color.swapChannels(ColorChannel.R, ColorChannel.B);
                    img.setPixelRGBA(i, k, color.getAsARGB());
                }
            }
            DynamicTexture tex = new DynamicTexture(img);
            return tex;
        });

        this.colorSliderTexture = new Cache<>(() -> {
            NativeImage img = new NativeImage((int)colorSliderArea.get().width(), (int)colorSliderArea.get().height(), false);
            if (sliderAlign.get().isHorizontal()) {
                for (int k = 0; k < (int)colorSliderArea.get().height(); k++) {
                    float val = 1f - ((float)k / (int)colorSliderArea.get().height());
                    DLColor color = HSVSlot.colorOf(this, hsvSlots.get().slider(), val);
                    color = color.swapChannels(ColorChannel.R, ColorChannel.B);
                    for (int i = 0; i < (int)colorSliderArea.get().width(); i++) {
                        img.setPixelRGBA(i, k, color.getAsARGB());
                    }
                }
            } else {
                for (int k = 0; k < (int)colorSliderArea.get().width(); k++) {
                    float val = 1f - ((float)k / (int)colorSliderArea.get().width());
                    DLColor color = HSVSlot.colorOf(this, hsvSlots.get().slider(), val);
                    color = color.swapChannels(ColorChannel.R, ColorChannel.B);
                    for (int i = 0; i < (int)colorSliderArea.get().height(); i++) {
                        img.setPixelRGBA(k, i, color.getAsARGB());
                    }
                }
            }
            return new DynamicTexture(img);
        });

        addEventListener(DLGuiStandardEvents.DragEvent.class, (s, e) -> {
            skipValueUpdate = true;
            if (quadArea.get().collision(e.localMouseOriginX(), e.localMouseOriginY())) {
                float relX = (float)((e.mouseX() - quadArea.get().x()) / quadArea.get().width());
                float relY = (float)((e.mouseY() - quadArea.get().y()) / quadArea.get().height());
                relX = MathUtils.clamp(relX, 0f, 1f);
                relY = MathUtils.clamp(relY, 0f, 1f);
                hsvSlots.get().quadX().set(this, relX);
                hsvSlots.get().quadY().set(this, 1f - relY);
                resetSliderTexture();
            } else if (colorSliderArea.get().collision(e.localMouseOriginX(), e.localMouseOriginY())) {
                float rel = 0;
                if (sliderAlign.get().isHorizontal()) {
                    rel = (float)((e.mouseY() - colorSliderArea.get().y()) / colorSliderArea.get().height());
                } else {                    
                    rel = (float)((e.mouseX() - colorSliderArea.get().x()) / colorSliderArea.get().width());
                }
                rel = MathUtils.clamp(rel, 0f, 1f);
                hsvSlots.get().slider().set(this, 1f - rel);
                resetQuadTexture();
            } else if (alphaSliderArea.get().collision(e.localMouseOriginX(), e.localMouseOriginY())) {
                float rel = 0;
                if (sliderAlign.get().isHorizontal()) {
                    rel = (float)((e.mouseY() - alphaSliderArea.get().y()) / alphaSliderArea.get().height());
                } else {                    
                    rel = (float)((e.mouseX() - alphaSliderArea.get().x()) / alphaSliderArea.get().width());
                }
                rel = MathUtils.clamp(rel, 0f, 1f);
                selectedAlpha = rel;
                resetQuadTexture();
            }
            color.set(DLColor.fromHsv(selectedHue * 360, selectedSaturation, selectedBrightness).withAlpha((int)(selectedAlpha * 255)));
            skipValueUpdate = false;
            return false;
        });
        
        
        addEventListener(DLGuiStandardEvents.MouseMoveEvent.class, (s, e) -> {
            if (quadArea.get().collision(e.mouseX(), e.mouseY()) || colorSliderArea.get().collision(e.mouseX(), e.mouseY()) || alphaSliderArea.get().collision(e.mouseX(), e.mouseY())) {
                cursor.set(CursorType.CROSSHAIR);
            } else {
                cursor.set(null);
            }
            return false;
        });

        addEventListener(ColorChangedEvent.class, (s, e) -> {
            if (skipValueUpdate) return false;
            this.selectedHue = color.get().getHue() / 360f;
            this.selectedBrightness = color.get().getBrightness();
            this.selectedSaturation = color.get().getSaturation();
            this.selectedAlpha = color.get().getAlphaF();
            updateScreenLayout();
            return false;
        });
        
    }

    @Override
    public Rectangle getRenderBounds() {
        Rectangle rect = super.getRenderBounds();
        return Rectangle.withPoints(rect.left() - 3, rect.top() - 3, rect.right() + 3, rect.bottom() + 3);
    }

    @Override
    public void updateScreenLayout() {
        resetQuadTexture();
        resetSliderTexture();
        quadArea.clear();
        colorSliderArea.clear();
        alphaSliderArea.clear();
    }

    private void resetQuadTexture() {
        if (quadTexture.isCached()) {
            quadTexture.get().close();
        }
        quadTexture.clear();
    }

    private void resetSliderTexture() {
        if (colorSliderTexture.isCached()) {
            colorSliderTexture.get().close();
        }
        colorSliderTexture.clear();
    }

    

    public int getQuadCursorX() {
        return (int)(((hsvSlots.get().quadX().get(this)) * quadArea.get().width()) + quadArea.get().x());
    }

    public int getQuadCursorY() {
        return (int)(((1f - hsvSlots.get().quadY().get(this)) * quadArea.get().height()) + quadArea.get().y());
    }

    public int getColorSliderCursor() {
        if (sliderAlign.get().isHorizontal()) {
            return (int)(((1f - hsvSlots.get().slider().get(this)) * colorSliderArea.get().height()) + colorSliderArea.get().y());
        }
        return (int)(((1f - hsvSlots.get().slider().get(this)) * colorSliderArea.get().width()) + colorSliderArea.get().x());
    }    

    public int getAlphaSliderCursor() {
        if (sliderAlign.get().isHorizontal()) {
            return (int)(((selectedAlpha) * alphaSliderArea.get().height()) + alphaSliderArea.get().y());
        }
        return (int)(((selectedAlpha) * alphaSliderArea.get().width()) + alphaSliderArea.get().x());
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite("slot").render(graphics, (int)quadArea.get().x() - 1, (int)quadArea.get().y() - 1, (int)quadArea.get().width() + 2, (int)quadArea.get().height() + 2);
        int w = quadTexture.get().getPixels().getWidth();
        int h = quadTexture.get().getPixels().getHeight();
        GuiUtils.drawTexture(quadTexture.get().getId(), graphics, (int)quadArea.get().x(), (int)quadArea.get().y(), w, h, 0, 0, w, h, TextureFillMode.STRETCH, w, h);

        if (showColorSlider.get()) {            
            DefaultGuiTextures.DRAGONLIB_UI.getSprite("slot").render(graphics, (int)colorSliderArea.get().x() - 1, (int)colorSliderArea.get().y() - 1, (int)colorSliderArea.get().width() + 2, (int)colorSliderArea.get().height() + 2);
            w = colorSliderTexture.get().getPixels().getWidth();
            h = colorSliderTexture.get().getPixels().getHeight();
            GuiUtils.drawTexture(colorSliderTexture.get().getId(), graphics, (int)colorSliderArea.get().x(), (int)colorSliderArea.get().y(), w, h, 0, 0, w, h, TextureFillMode.STRETCH, w, h);
        }
        
        if (showAlphaSlider.get()) {            
            DefaultGuiTextures.DRAGONLIB_UI.getSprite("slot").render(graphics, (int)alphaSliderArea.get().x() - 1, (int)alphaSliderArea.get().y() - 1, (int)alphaSliderArea.get().width() + 2, (int)alphaSliderArea.get().height() + 2);
            DefaultGuiTextures.DRAGONLIB_UI.getSprite("transparency").render(graphics, (int)alphaSliderArea.get().x(), (int)alphaSliderArea.get().y(), (int)alphaSliderArea.get().width(), (int)alphaSliderArea.get().height());
            if (sliderAlign.get().isHorizontal()) {
                GuiUtils.fillGradient(graphics, alphaSliderArea.get(), DLColor.TRANSPARENT, color.get().withAlpha(255), EAlign.TOP);
            } else {
                GuiUtils.fillGradient(graphics, alphaSliderArea.get(), DLColor.TRANSPARENT, color.get().withAlpha(255), EAlign.LEFT);
            }
        }

        GuiUtils.drawBox(graphics, getQuadCursorX() - 3, getQuadCursorY() - 3, 6, 6, color.get(), DLColor.pickBasedOnBrightness(color.get(), DLColor.WHITE, DLColor.BLACK, 0.5f));
        
        if (sliderAlign.get().isHorizontal()) {
            if (showColorSlider.get()) GuiUtils.drawBox(graphics, (int)colorSliderArea.get().left() - 3, (int)getColorSliderCursor() - 3, (int)colorSliderArea.get().width() + 6, 6, hsvSlots.get().slider().colorOf(this), DLColor.BLACK);
            if (showAlphaSlider.get()) GuiUtils.drawBox(graphics, (int)alphaSliderArea.get().left() - 3, (int)getAlphaSliderCursor() - 3, (int)alphaSliderArea.get().width() + 6, 6, color.get().withAlpha((int)(selectedAlpha * 255)), DLColor.BLACK);
        } else {
            if (showColorSlider.get()) GuiUtils.drawBox(graphics, (int)getColorSliderCursor() - 3, (int)colorSliderArea.get().top() - 3, 6, (int)colorSliderArea.get().height() + 6, hsvSlots.get().slider().colorOf(this), DLColor.BLACK);
            if (showAlphaSlider.get()) GuiUtils.drawBox(graphics, (int)getAlphaSliderCursor() - 3, (int)alphaSliderArea.get().top() - 3, 6, (int)alphaSliderArea.get().height() + 6, color.get().withAlpha((int)(selectedAlpha * 255)), DLColor.BLACK);
        }

    }

    @Override
    public void renderFrontLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (isSelected() && showAlphaSlider.get() && alphaSliderArea.get().collision(mouseX, mouseY)) {
            GuiUtils.drawTooltip(graphics, graphics.defaultFont(), (int)mouseX, (int)mouseY, List.of(TextUtils.translate("gui." + DragonLib.MODID + ".opacity").append(" " + (int)(100 * selectedAlpha) + "%")), (int)getWindowManager().getScreenWidth());
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        resetSliderTexture();
        resetQuadTexture();
    }

    
}
