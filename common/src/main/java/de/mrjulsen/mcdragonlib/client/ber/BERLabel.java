package de.mrjulsen.mcdragonlib.client.ber;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.Vector3f;

import com.mojang.blaze3d.font.GlyphInfo;

import de.mrjulsen.mcdragonlib.client.util.DLGraphics;
import de.mrjulsen.mcdragonlib.client.util.FontUtils;
import de.mrjulsen.mcdragonlib.client.util.RenderUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.mixin.BakedGlyphAccessor;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.Pair;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;

public class BERLabel {

    public enum EScrollMode { NEVER, WHEN_NEEDED, ALWAYS }

    protected static record StyledChar(
        CharData charData,
        Style style,
        float unscaledAdvance
    ) {}

    protected static record CharData(
        int charCode,
        String charString,
        Style style,
        GlyphInfo glyphInfo,
        BakedGlyphAccessor glyph,
        float glyphUWidth,
        float glyphVHeight
    ) {}

    protected static final Map<Integer, CharData> charDataCache = new ConcurrentHashMap<>();

    public final Property<Rectangle> bounds = new Property<>(Rectangle.withSize(0, 0, 16, 16));
    public final NumberProperty<Float> x = new NumberProperty<>(0F);
    public final NumberProperty<Float> y = new NumberProperty<>(0F);
    public final Property<ETextAlignment> textAlign = new Property<>(ETextAlignment.LEFT);

    public final Property<Component> text = new Property<>(TextUtils.empty());
    public final ColorProperty color = new ColorProperty(DLColor.WHITE, DLColor.WHITE);
    public final ColorProperty backgroundColor = new ColorProperty(DLColor.TRANSPARENT, DLColor.TRANSPARENT);
    public final BooleanProperty fullBackground = new BooleanProperty(false, false);

    public final NumberProperty<Float> targetWidth = new NumberProperty<>(16F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> targetHeight = new NumberProperty<>(16F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> horizontalMinScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> horizontalMaxScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> verticalMinScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> verticalMaxScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);

    public final NumberProperty<Float> horizontalScrollingSpeed = new NumberProperty<>(4F);
    public final NumberProperty<Float> verticalScrollingSpeed = new NumberProperty<>(4F);
    public final Property<EScrollMode> horizontalScrollMode = new Property<>(EScrollMode.NEVER);
    public final Property<EScrollMode> verticalScrollMode = new Property<>(EScrollMode.NEVER);

    private final FontUtils fontUtils = new FontUtils(Style.DEFAULT_FONT);

    private long lastRenderTime = 0;
    private float horizontalScrollOffset = 0.0f;
    private float verticalScrollOffset = 0.0f;

    private List<Pair<CharData, Float>> cachedGlyphs = null;
    private float cachedUnscaledTextWidth = 0f;
    private Component lastRenderedText = null;

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
        return charDataCache.computeIfAbsent(codePoint, c -> {
            GlyphInfo info = fontUtils.fontSet.getGlyphInfo(c, false);
            BakedGlyphAccessor glyph = fontUtils.getGlyphAccessor(c);
            float glyphUVWidth = glyph.dragonlib$getU1() - glyph.dragonlib$getU0();
            float glyphUVHeight = glyph.dragonlib$getV1() - glyph.dragonlib$getV0();
            return new CharData(c, String.valueOf(Character.toChars(c)), style, info, glyph, glyphUVWidth, glyphUVHeight);
        });
    }

    private void updateGlyphCache(Component component) {
        if (component == null || component.equals(lastRenderedText)) return;

        List<Pair<CharData, Float>> glyphs = new ArrayList<>();
        MutableFloat widthSum = new MutableFloat();

        StringDecomposer.iterateFormatted(component, Style.EMPTY, (charIndex, styl, codePoint) -> {
            CharData data = getCharData(codePoint, styl);
            float adv = data.glyphInfo().getAdvance(styl.isBold());
            glyphs.add(Pair.of(data, adv));
            widthSum.add(adv);
            return true;
        });

        cachedGlyphs = glyphs;
        cachedUnscaledTextWidth = widthSum.getValue();
        lastRenderedText = component;
    }

    public void render(DLGraphics graphics) {
        Component component = text.get();
        if (component == null || component.getString().isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (lastRenderTime == 0) ? 0 : (currentTime - lastRenderTime) / 1000.0f;
        lastRenderTime = currentTime;

        final float charQuadSize = 8.0f;

        updateGlyphCache(component);

        float targetW = targetWidth.get();
        float targetH = targetHeight.get();
        float textX = x.get();
        float textY = y.get();

        float minHScale = Math.max(horizontalMinScale.get(), 0.0001f);
        float maxHScale = horizontalMaxScale.get() <= 0 ? Float.MAX_VALUE : horizontalMaxScale.get();
        float minVScale = Math.max(verticalMinScale.get(), 0.0001f);
        float maxVScale = verticalMaxScale.get() <= 0 ? Float.MAX_VALUE : verticalMaxScale.get();

        float baseHScale = Mth.clamp(1.0f, minHScale, maxHScale);
        float textWidthAtBaseScale = cachedUnscaledTextWidth * baseHScale;
        boolean hScrollNeeded = targetW > 0 && textWidthAtBaseScale > targetW;

        float baseVScale = Mth.clamp(1.0f, minVScale, maxVScale);
        float textHeightAtBaseScale = charQuadSize * baseVScale;
        boolean vScrollNeeded = targetH > 0 && textHeightAtBaseScale > targetH;

        EScrollMode hMode = horizontalScrollMode.get();
        boolean hScrollActive = horizontalScrollingSpeed.get() != 0.0f && (hMode == EScrollMode.ALWAYS || (hMode == EScrollMode.WHEN_NEEDED && hScrollNeeded));

        EScrollMode vMode = verticalScrollMode.get();
        boolean vScrollActive = verticalScrollingSpeed.get() != 0.0f && (vMode == EScrollMode.ALWAYS || (vMode == EScrollMode.WHEN_NEEDED && vScrollNeeded));

        Rectangle initialBounds = bounds.get();
        float finalClipLeft = (float)(hScrollActive ? Math.max(initialBounds.left(), textX) : initialBounds.left());
        float finalClipRight = (float)(hScrollActive ? (targetW > 0 ? Math.min(initialBounds.right(), textX + targetW) : initialBounds.right()) : initialBounds.right());
        float finalClipTop = (float)(vScrollActive ? Math.max(initialBounds.top(), textY) : initialBounds.top());
        float finalClipBottom = (float)(vScrollActive ? (targetH > 0 ? Math.min(initialBounds.bottom(), textY + targetH) : initialBounds.bottom()) : initialBounds.bottom());

        Rectangle finalClipBounds = Rectangle.withSize(finalClipLeft, finalClipTop, Math.max(0, finalClipRight - finalClipLeft), Math.max(0, finalClipBottom - finalClipTop));

        float finalHScale, finalVScale;

        if (hScrollActive) {
            finalHScale = baseHScale;
            float totalScrollRange = (float)finalClipBounds.width() + (cachedUnscaledTextWidth * finalHScale);
            horizontalScrollOffset += horizontalScrollingSpeed.get() * deltaTime;
            horizontalScrollOffset %= totalScrollRange;
        } else {
            finalHScale = targetW > 0 && cachedUnscaledTextWidth > 0 ? (targetW / cachedUnscaledTextWidth) : 1.0f;
            finalHScale = Mth.clamp(finalHScale, minHScale, maxHScale);
            horizontalScrollOffset = 0;
        }

        if (vScrollActive) {
            finalVScale = baseVScale;
            float totalScrollRange = (float)finalClipBounds.height() + (charQuadSize * finalVScale);
            verticalScrollOffset += verticalScrollingSpeed.get() * deltaTime;
            verticalScrollOffset %= totalScrollRange;
        } else {
            finalVScale = targetH > 0 && charQuadSize > 0 ? (targetH / charQuadSize) : 1.0f;
            finalVScale = Mth.clamp(finalVScale, minVScale, maxVScale);
            verticalScrollOffset = 0;
        }

        float totalScaledWidth = cachedUnscaledTextWidth * finalHScale;
        float totalScaledHeight = charQuadSize * finalVScale;

        float finalX = (float)(hScrollActive ? finalClipBounds.right() - horizontalScrollOffset : textX);
        float finalY = (float)(vScrollActive ? finalClipBounds.bottom() - verticalScrollOffset : textY);

        if (!hScrollActive) {
            ETextAlignment alignment = textAlign.get();
            if (alignment == ETextAlignment.CENTER) finalX -= totalScaledWidth / 2.0f;
            else if (alignment == ETextAlignment.RIGHT) finalX -= totalScaledWidth;
        }

        DLColor bgColor = backgroundColor.get();
        if (bgColor != null && bgColor.getAlphaF() >= 1) {
            float bgX, bgY, bgW, bgH;

            if (fullBackground.get()) {
                bgX = (float)finalClipBounds.left();
                bgY = (float)finalClipBounds.top();
                bgW = (float)finalClipBounds.width();
                bgH = (float)finalClipBounds.height();
            } else {
                float unclippedX = finalX - 1.0f;
                float unclippedY = finalY - 1.0f;
                float unclippedW = totalScaledWidth + 2.0f;
                float unclippedH = totalScaledHeight + 2.0f;

                bgX = (float)Math.max(unclippedX, finalClipBounds.left());
                bgY = (float)Math.max(unclippedY, finalClipBounds.top());
                bgW = (float)Math.max(0, Math.min(unclippedX + unclippedW, finalClipBounds.right()) - bgX);
                bgH = (float)Math.max(0, Math.min(unclippedY + unclippedH, finalClipBounds.bottom()) - bgY);
            }

            if (bgW > 0 && bgH > 0)
                RenderUtils.fillColor(graphics, new Vector3f(bgX, bgY, 0.0f), bgW, bgH, bgColor, Direction.NORTH, graphics.packedLight(), false);
        }

        graphics.poseStack().pushPose();
        graphics.poseStack().translate(finalX, finalY, 0.001);

        float currentUnscaledX = 0f;
        for (Pair<CharData, Float> pair : cachedGlyphs) {
            CharData charData = pair.getFirst();
            float glyphAdvance = pair.getSecond();

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

            Font.StringRenderOutput sro = fontUtils.font.new StringRenderOutput(
                graphics.multiBufferSource(),
                0, 0,
                color.get().getAsARGB(),
                false,
                graphics.poseStack().last().pose(),
                Font.DisplayMode.NORMAL,
                graphics.packedLight()
            );
            StringDecomposer.iterateFormatted(charData.charString(), charData.style(), sro);

            graphics.poseStack().popPose();
            fontUtils.popUV(charData.charCode());

            currentUnscaledX += glyphAdvance;
        }

        graphics.poseStack().popPose();
    }
}
