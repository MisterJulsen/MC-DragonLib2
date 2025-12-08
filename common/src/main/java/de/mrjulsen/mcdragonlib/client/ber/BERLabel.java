package de.mrjulsen.mcdragonlib.client.ber;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.Vector3f;
import com.mojang.blaze3d.font.GlyphInfo;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.PaddingF;
import de.mrjulsen.mcdragonlib.client.util.DLGraphics;
import de.mrjulsen.mcdragonlib.client.util.FontUtils;
import de.mrjulsen.mcdragonlib.client.util.RenderUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.mixin.BakedGlyphAccessor;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.Pair;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Point;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import de.mrjulsen.mcdragonlib.util.properties.VirtualProperty;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;

public class BERLabel {

    public enum EScrollMode { NEVER, WHEN_NEEDED, ALWAYS, FLEX_FIT }

    protected static record StyledChar(
        CharData charData,
        Style style,
        float unscaledAdvance
    ) {}

    protected static record CharData(
        int charCode,
        String charString,
        boolean isBold,
        GlyphInfo glyphInfo,
        BakedGlyphAccessor glyph,
        float glyphUWidth,
        float glyphVHeight
    ) {}

    protected static final Map<Integer, CharData> charDataCache = new ConcurrentHashMap<>();

    public final Property<Rectangle> clippingArea = new Property<>(Rectangle.withSize(0, 0, Short.MAX_VALUE, Short.MAX_VALUE));
    public final NumberProperty<Float> x = new NumberProperty<>(0F);
    public final NumberProperty<Float> y = new NumberProperty<>(0F);
    public final VirtualProperty<Point> position = new VirtualProperty<Point>(Point.of(0, 0),
        () -> Point.of(x.get(), y.get()),
        (p) -> {
            x.set((float)p.x());
            y.set((float)p.y());
        });

    public final Property<ETextAlignment> horizontalAlign = new Property<>(ETextAlignment.LEFT);

    public final Property<Component> text = new Property<>(TextUtils.empty());
    public final ColorProperty color = new ColorProperty(DLColor.WHITE, DLColor.WHITE);
    public final ColorProperty backgroundColor = new ColorProperty(DLColor.TRANSPARENT, DLColor.TRANSPARENT);
    
    public final Property<PaddingF> backgroundPadding = new Property<>(new PaddingF(1, 1, 1, 1));
    public final BooleanProperty fullBackground = new BooleanProperty(false);
    public final BooleanProperty glowing = new BooleanProperty(false);

    public final NumberProperty<Float> preferredWidth = new NumberProperty<>(16F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> preferredHeight = new NumberProperty<>(16F, 0F, (float) Integer.MAX_VALUE);     
    public final VirtualProperty<Size> preferredSize = new VirtualProperty<Size>(Size.of(0F, 0F),
        () -> Size.of(preferredWidth.get(), preferredHeight.get()),
        (p) -> {
            preferredWidth.set((float)p.w());
            preferredHeight.set((float)p.h());
        });             
    public final VirtualProperty<Rectangle> layoutArea = new VirtualProperty<Rectangle>(Rectangle.withSize(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE),
        () -> Rectangle.withSize(x.get(), y.get(), preferredWidth.get(), preferredHeight.get()),
        (r) -> {
            x.set((float)r.x());
            y.set((float)r.y());
            preferredWidth.set((float)r.width());
            preferredHeight.set((float)r.height());
        });

    public final NumberProperty<Float> horizontalMinScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> horizontalMaxScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final VirtualProperty<Pair<Float, Float>> horizontalScale = new VirtualProperty<Pair<Float, Float>>(Pair.of(0F, 0F),
        () -> Pair.of(horizontalMinScale.get(), horizontalMaxScale.get()),
        (p) -> {
            horizontalMinScale.set((float)p.getFirst());
            horizontalMaxScale.set((float)p.getSecond());
        });
    public final NumberProperty<Float> verticalMinScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> verticalMaxScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final VirtualProperty<Pair<Float, Float>> verticalScale = new VirtualProperty<Pair<Float, Float>>(Pair.of(0F, 0F),
        () -> Pair.of(verticalMinScale.get(), verticalMaxScale.get()),
        (p) -> {
            verticalMinScale.set((float)p.getFirst());
            verticalMaxScale.set((float)p.getSecond());
        });

    public final NumberProperty<Float> horizontalScrollingSpeed = new NumberProperty<>(4F);
    public final NumberProperty<Float> verticalScrollingSpeed = new NumberProperty<>(4F);
    public final Property<EScrollMode> horizontalScrollMode = new Property<>(EScrollMode.NEVER);
    public final Property<EScrollMode> verticalScrollMode = new Property<>(EScrollMode.NEVER);

    private final FontUtils fontUtils = new FontUtils(Style.DEFAULT_FONT);

    private long lastRenderTime = 0;
    private float horizontalScrollOffset = 0.0f;
    private float verticalScrollOffset = 0.0f;

    private List<StyledChar> cachedGlyphs = null;
    private float cachedUnscaledTextWidth = 0f;
    private Component lastRenderedText = null;
    
    private float cachedFinalHScale = 1.0f;
    private float cachedFinalVScale = 1.0f;

    public BERLabel() {
        charDataCache.clear();
        text.withAfterPropertyChangedCallback((oldVal, newVal) -> invalidateCache());
    }

    private void invalidateCache() {
        cachedGlyphs = null;
        cachedUnscaledTextWidth = 0f;
        lastRenderedText = null;
    }    

    private CharData getCharData(int codePoint, Style style) {
        int key = codePoint;
        if (style.isBold()) key |= (1 << 24); 

        return charDataCache.computeIfAbsent(key, c -> {
            boolean isBold = (c & (1 << 24)) != 0;
            GlyphInfo info = fontUtils.fontSet.getGlyphInfo(codePoint, isBold);
            BakedGlyphAccessor glyph = fontUtils.getGlyphAccessor(codePoint);
            float glyphUVWidth = glyph.dragonlib$getU1() - glyph.dragonlib$getU0();
            float glyphUVHeight = glyph.dragonlib$getV1() - glyph.dragonlib$getV0();
            return new CharData(codePoint, String.valueOf(Character.toChars(codePoint)), isBold, info, glyph, glyphUVWidth, glyphUVHeight);
        });
    }

    private void updateGlyphCache(Component component) {
        if (component == null || component.equals(lastRenderedText)) return;

        List<StyledChar> glyphs = new ArrayList<>();
        MutableFloat widthSum = new MutableFloat();

        StringDecomposer.iterateFormatted(component, Style.EMPTY, (charIndex, styl, codePoint) -> {
            CharData data = getCharData(codePoint, styl);
            float adv = data.glyphInfo().getAdvance(styl.isBold());
            glyphs.add(new StyledChar(data, styl, adv));
            widthSum.add(adv);
            return true;
        });

        cachedGlyphs = glyphs;
        cachedUnscaledTextWidth = widthSum.getValue();
        lastRenderedText = component;
    }

    private void calculateFinalScales() {
        final float charQuadSize = 8.0f;
        float targetW = preferredWidth.get();
        float targetH = preferredHeight.get();
        
        float minHScale = Math.max(horizontalMinScale.get(), 0.0001f);
        float maxHScale = horizontalMaxScale.get() <= 0 ? Float.MAX_VALUE : horizontalMaxScale.get();
        float minVScale = Math.max(verticalMinScale.get(), 0.0001f);
        float maxVScale = verticalMaxScale.get() <= 0 ? Float.MAX_VALUE : verticalMaxScale.get();

        float baseHScale = Mth.clamp(1.0f, minHScale, maxHScale);
        float baseVScale = Mth.clamp(1.0f, minVScale, maxVScale);
        float textHeightAtBaseScale = charQuadSize * baseVScale;
        boolean vScrollNeeded = targetH > 0 && textHeightAtBaseScale > targetH;
        
        EScrollMode hMode = horizontalScrollMode.get();
        boolean hScrollActive;

        if (hMode == EScrollMode.ALWAYS) {
            hScrollActive = horizontalScrollingSpeed.get() != 0.0f;
        } else {
            float idealScale = targetW > 0 && cachedUnscaledTextWidth > 0 ? (targetW / cachedUnscaledTextWidth) : 1.0f;
            
            if (hMode == EScrollMode.WHEN_NEEDED && idealScale < minHScale) {
                hScrollActive = horizontalScrollingSpeed.get() != 0.0f;
            } else {
                hScrollActive = false;
            }
        }
        
        if (hScrollActive) {
            cachedFinalHScale = baseHScale;
        } else {
            float idealScale = targetW > 0 && cachedUnscaledTextWidth > 0 ? (targetW / cachedUnscaledTextWidth) : 1.0f;
            
            if (hMode == EScrollMode.FLEX_FIT && idealScale < minHScale) {
                cachedFinalHScale = minHScale;
            } else {
                cachedFinalHScale = Mth.clamp(idealScale, minHScale, maxHScale);
            }
        }

        EScrollMode vMode = verticalScrollMode.get();
        boolean vScrollActive = verticalScrollingSpeed.get() != 0.0f && (vMode == EScrollMode.ALWAYS || (vMode == EScrollMode.WHEN_NEEDED && vScrollNeeded));
        
        if (vScrollActive) {
            cachedFinalVScale = baseVScale;
        } else {
            float idealScale = targetH > 0 && charQuadSize > 0 ? (targetH / charQuadSize) : 1.0f;
            cachedFinalVScale = Mth.clamp(idealScale, minVScale, maxVScale);
        }
    }

    public void render(DLGraphics graphics) {
        render(graphics, glowing.get() ? LightTexture.FULL_BRIGHT : graphics.packedLight());
    }

    public void render(DLGraphics graphics, int light) {
        Component component = text.get();
        if (component == null || component.getString().isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (lastRenderTime == 0) ? 0 : (currentTime - lastRenderTime) / 1000.0f;
        lastRenderTime = currentTime;

        final float charQuadSize = 8.0f;

        updateGlyphCache(component);
        calculateFinalScales();

        float targetW = preferredWidth.get();
        float targetH = preferredHeight.get();
        float textX = x.get();
        float textY = y.get();

        float minHScale = Math.max(horizontalMinScale.get(), 0.0001f);
        float minVScale = Math.max(verticalMinScale.get(), 0.0001f);
        float baseVScale = Mth.clamp(1.0f, minVScale, verticalMaxScale.get() <= 0 ? Float.MAX_VALUE : verticalMaxScale.get());
        float textHeightAtBaseScale = charQuadSize * baseVScale;
        boolean vScrollNeeded = targetH > 0 && textHeightAtBaseScale > targetH;

        Rectangle initialBounds = clippingArea.get();
        float finalClipLeft = (float)Math.max(initialBounds.left(), textX);
        float finalClipRight = (float)(targetW > 0 ? Math.min(initialBounds.right(), textX + targetW) : initialBounds.right());
        float finalClipTop = (float)Math.max(initialBounds.top(), textY);
        float finalClipBottom = (float)(targetH > 0 ? Math.min(initialBounds.bottom(), textY + targetH) : initialBounds.bottom());

        EScrollMode hMode = horizontalScrollMode.get();
        float finalHScale = cachedFinalHScale;

        boolean hScrollActive;
        if (hMode == EScrollMode.ALWAYS) {
            hScrollActive = horizontalScrollingSpeed.get() != 0.0f;
        } else {
            float idealScale = targetW > 0 && cachedUnscaledTextWidth > 0 ? (targetW / cachedUnscaledTextWidth) : 1.0f;
            hScrollActive = (hMode == EScrollMode.WHEN_NEEDED && idealScale < minHScale) && horizontalScrollingSpeed.get() != 0.0f;
        }
        
        if (hScrollActive) {
            finalClipLeft = (float)Math.max(initialBounds.left(), textX);
            finalClipRight = (float)(targetW > 0 ? Math.min(initialBounds.right(), textX + targetW) : initialBounds.right());
            
            float totalScrollRange = (float)Math.max(0, finalClipRight - finalClipLeft) + (cachedUnscaledTextWidth * finalHScale);
            horizontalScrollOffset += horizontalScrollingSpeed.get() * deltaTime;
            horizontalScrollOffset %= totalScrollRange;
        } else {
            if (hMode == EScrollMode.FLEX_FIT && (targetW > 0 && cachedUnscaledTextWidth > 0) && (targetW / cachedUnscaledTextWidth) < minHScale) {
                finalClipRight = (float)initialBounds.right();
            }
            horizontalScrollOffset = 0;
        }

        EScrollMode vMode = verticalScrollMode.get();
        float finalVScale = cachedFinalVScale;
        boolean vScrollActive = verticalScrollingSpeed.get() != 0.0f && (vMode == EScrollMode.ALWAYS || (vMode == EScrollMode.WHEN_NEEDED && vScrollNeeded));
        
        if (vScrollActive) {
            finalClipTop = (float)Math.max(initialBounds.top(), textY);
            finalClipBottom = (float)(targetH > 0 ? Math.min(initialBounds.bottom(), textY + targetH) : initialBounds.bottom());
            
            float totalScrollRange = (float)Math.max(0, finalClipBottom - finalClipTop) + (charQuadSize * finalVScale);
            verticalScrollOffset += verticalScrollingSpeed.get() * deltaTime;
            verticalScrollOffset %= totalScrollRange;
        } else {
            verticalScrollOffset = 0;
        }
        
        Rectangle finalClipBounds = Rectangle.withSize(finalClipLeft, finalClipTop, Math.max(0, finalClipRight - finalClipLeft), Math.max(0, finalClipBottom - finalClipTop));

        if (targetW <= 0.0f || targetH <= 0.0f || finalClipBounds.width() <= 0 || finalClipBounds.height() <= 0) {
            return;
        }

        float totalScaledWidth = cachedUnscaledTextWidth * finalHScale;
        float totalScaledHeight = charQuadSize * finalVScale;

        float finalX = (float)(hScrollActive ? (float)finalClipBounds.right() - horizontalScrollOffset : textX);
        float finalY = (float)(vScrollActive ? (float)finalClipBounds.bottom() - verticalScrollOffset : textY);

        if (!hScrollActive) {
            ETextAlignment alignment = horizontalAlign.get();
            
            if (targetW > 0) {
                if (alignment == ETextAlignment.CENTER) {
                    finalX = textX + (targetW - totalScaledWidth) / 2.0f;
                } else if (alignment == ETextAlignment.RIGHT) {
                    finalX = textX + targetW - totalScaledWidth;
                }
            } else {
                if (alignment == ETextAlignment.CENTER) finalX -= totalScaledWidth / 2.0f;
                else if (alignment == ETextAlignment.RIGHT) finalX -= totalScaledWidth;
            }
        }

        DLColor bgColor = backgroundColor.get();
        if (bgColor != null && bgColor.getAlphaF() > 0) {
            PaddingF padding = backgroundPadding.get();
            
            float bgX, bgY, bgW, bgH;

            if (fullBackground.get()) {
                bgX = textX;
                bgY = textY;
                
                bgW = targetW;
                if (bgW <= 0 || (hMode == EScrollMode.FLEX_FIT && totalScaledWidth > bgW)) {
                    bgW = totalScaledWidth;
                }

                bgH = targetH;
                if (bgH <= 0 || (vMode == EScrollMode.FLEX_FIT && totalScaledHeight > bgH)) {
                    bgH = totalScaledHeight;
                }
            } else {
                bgX = finalX;
                bgY = finalY;
                bgW = totalScaledWidth;
                bgH = totalScaledHeight;
            }

            float drawX = bgX - padding.left();
            float drawY = bgY - padding.top();
            float drawW = bgW + padding.left() + padding.right();
            float drawH = bgH + padding.top() + padding.bottom();

            float visibleX = Math.max(drawX, (float)Math.max(finalClipBounds.left() - padding.left(), initialBounds.left()));
            float visibleY = Math.max(drawY, (float)Math.max(finalClipBounds.top() - padding.top(), initialBounds.top()));
            float visibleRight = Math.min(drawX + drawW, (float)Math.min(finalClipBounds.right() + padding.right(), initialBounds.right()));
            float visibleBottom = Math.min(drawY + drawH, (float)Math.min(finalClipBounds.bottom() + padding.bottom(), initialBounds.bottom()));
            
            float visibleW = visibleRight - visibleX;
            float visibleH = visibleBottom - visibleY;

            if (visibleW > 0 && visibleH > 0) {
                RenderUtils.fillColor(graphics, new Vector3f(visibleX, visibleY, 0.0f), visibleW, visibleH, bgColor, Direction.NORTH, light, false);
            }
            
            /*
            RenderUtils.drawDebugLine(graphics, new Vector3f((float)visibleX, visibleY, 0), new Vector3f(visibleRight, visibleY, 0), DLColor.GREEN);
            RenderUtils.drawDebugLine(graphics, new Vector3f((float)visibleX, visibleY, 0), new Vector3f((float)visibleX, visibleBottom, 0), DLColor.GREEN);
            RenderUtils.drawDebugLine(graphics, new Vector3f(visibleRight, visibleY, 0), new Vector3f(visibleRight, visibleBottom, 0), DLColor.GREEN);
            RenderUtils.drawDebugLine(graphics, new Vector3f((float)visibleX, visibleBottom, 0), new Vector3f(visibleRight,visibleBottom, 0), DLColor.GREEN);
            */
        }

        graphics.poseStack().pushPose();
        graphics.poseStack().translate(finalX, finalY, 0.001);

        float currentUnscaledX = 0f;
        for (StyledChar styledChar : cachedGlyphs) {
            CharData charData = styledChar.charData();
            Style style = styledChar.style();
            float glyphAdvance = styledChar.unscaledAdvance();

            float quadWidth = charQuadSize * finalHScale;
            float quadHeight = charQuadSize * finalVScale;
            float quadWorldX = finalX + currentUnscaledX * finalHScale;
            float quadWorldY = finalY;

            if (quadWorldX + quadWidth < finalClipBounds.left() || quadWorldX > finalClipBounds.right() ||
                quadWorldY + quadHeight < finalClipBounds.top() || quadWorldY > finalClipBounds.bottom()) {
                currentUnscaledX += glyphAdvance;
                continue;
            }

            float u0Offset = 0f, u1Offset = 0f, v0Offset = 0f, v1Offset = 0f;
            float clipXLeft = 0f, clipXRight = 0f, clipYTop = 0f, clipYBottom = 0f;

            float glyphUWidth = charData.glyphUWidth();
            float glyphVHeight = charData.glyphVHeight();

            if (quadWorldX < finalClipBounds.left()) { clipXLeft = (float)(finalClipBounds.left() - quadWorldX) / quadWidth; u0Offset = glyphUWidth * clipXLeft; }
            if (quadWorldX + quadWidth > finalClipBounds.right()) { clipXRight = (float)(quadWorldX + quadWidth - finalClipBounds.right()) / quadWidth; u1Offset = glyphUWidth * clipXRight; }
            if (quadWorldY < finalClipBounds.top()) { clipYTop = (float)(finalClipBounds.top() - quadWorldY) / quadHeight; v0Offset = glyphVHeight * clipYTop; }
            if (quadWorldY + quadHeight > finalClipBounds.bottom()) { clipYBottom = (float)(quadWorldY + quadHeight - finalClipBounds.bottom()) / quadHeight; v1Offset = glyphVHeight * clipYBottom; }

            fontUtils.pushUV(charData.charCode());
            BakedGlyphAccessor glyph = charData.glyph();
            glyph.dragonlib$setU0(glyph.dragonlib$getU0() + u0Offset);
            glyph.dragonlib$setU1(glyph.dragonlib$getU1() - u1Offset);
            glyph.dragonlib$setV0(glyph.dragonlib$getV0() + v0Offset);
            glyph.dragonlib$setV1(glyph.dragonlib$getV1() - v1Offset);

            graphics.poseStack().pushPose();
            graphics.poseStack().scale(finalHScale, finalVScale, 1.0f);
            graphics.poseStack().translate(currentUnscaledX, 0, 0);

            graphics.poseStack().translate(charQuadSize * clipXLeft, charQuadSize * clipYTop, 0);
            graphics.poseStack().scale(1.0f - clipXLeft - clipXRight, 1.0f - clipYTop - clipYBottom, 1.0f);

            int finalColor = color.get().getAsARGB();
            if (style.getColor() != null) {
                finalColor = style.getColor().getValue() | 0xFF000000;
            }

            Font.StringRenderOutput sro = fontUtils.font.new StringRenderOutput(
                graphics.multiBufferSource(),
                0, 0,
                finalColor,
                style.isObfuscated(),
                graphics.poseStack().last().pose(),
                Font.DisplayMode.NORMAL,
                light
            );
            StringDecomposer.iterateFormatted(charData.charString(), style, sro);

            graphics.poseStack().popPose();
            fontUtils.popUV(charData.charCode());

            currentUnscaledX += glyphAdvance;
        }

        graphics.poseStack().popPose();

        /*
        RenderUtils.drawDebugLine(graphics, new Vector3f((float)initialBounds.x(), (float)initialBounds.y(), 0), new Vector3f((float)initialBounds.right(), (float)initialBounds.y(), 0), DLColor.RED);
        RenderUtils.drawDebugLine(graphics, new Vector3f((float)initialBounds.x(), (float)initialBounds.y(), 0), new Vector3f((float)initialBounds.x(), (float)initialBounds.bottom(), 0), DLColor.RED);
        RenderUtils.drawDebugLine(graphics, new Vector3f((float)initialBounds.right(), (float)initialBounds.y(), 0), new Vector3f((float)initialBounds.right(), (float)initialBounds.bottom(), 0), DLColor.RED);
        RenderUtils.drawDebugLine(graphics, new Vector3f((float)initialBounds.x(), (float)initialBounds.bottom(), 0), new Vector3f((float)initialBounds.right(), (float)initialBounds.bottom(), 0), DLColor.RED);
        
        RenderUtils.drawDebugLine(graphics, new Vector3f((float)finalClipBounds.x(), (float)finalClipBounds.y(), 0), new Vector3f((float)finalClipBounds.right(), (float)finalClipBounds.y(), 0), DLColor.YELLOW);
        RenderUtils.drawDebugLine(graphics, new Vector3f((float)finalClipBounds.x(), (float)finalClipBounds.y(), 0), new Vector3f((float)finalClipBounds.x(), (float)finalClipBounds.bottom(), 0), DLColor.YELLOW);
        RenderUtils.drawDebugLine(graphics, new Vector3f((float)finalClipBounds.right(), (float)finalClipBounds.y(), 0), new Vector3f((float)finalClipBounds.right(), (float)finalClipBounds.bottom(), 0), DLColor.YELLOW);
        RenderUtils.drawDebugLine(graphics, new Vector3f((float)finalClipBounds.x(), (float)finalClipBounds.bottom(), 0), new Vector3f((float)finalClipBounds.right(), (float)finalClipBounds.bottom(), 0), DLColor.YELLOW);
        */
    }

    public float getRenderedWidth() {
        if (this.cachedGlyphs == null) {
            updateGlyphCache(this.text.get());
        }
        calculateFinalScales();
        
        return this.cachedUnscaledTextWidth * this.cachedFinalHScale;
    }

    public float getRenderedHeight() {
        if (this.cachedGlyphs == null) {
            updateGlyphCache(this.text.get());
        }
        calculateFinalScales();
        
        return 8.0f * this.cachedFinalVScale;
    }
}