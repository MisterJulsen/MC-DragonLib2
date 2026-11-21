/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.client.ber;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.PaddingF;
import de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource;
import de.mrjulsen.mcdragonlib.client.util.DLGraphics;
import de.mrjulsen.mcdragonlib.client.util.RenderUtils;
import de.mrjulsen.mcdragonlib.client.util.SmoothTextDeferred;
import de.mrjulsen.mcdragonlib.client.util.SmoothTextRenderTypes;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.data.EVerticalAlignment;
import de.mrjulsen.mcdragonlib.mixin.client.FontAccessor;
import de.mrjulsen.mcdragonlib.mixin.client.StringSplitterAccessor;
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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

/**
 * A single line of text for block entity renderers with alignment, fit-scaling, scrolling, clipping and background.
 * <p>
 * All coordinates are in the local space of the pose stack passed to {@link #render(DLGraphics)}.
 * Rendering cost per frame is independent of the text length: only the visible part of the text is passed to the font.
 */
public class BERLabel {

    public enum EScrollMode {
        /** Text is scaled down to the min scale and clipped. */
        NEVER,
        /** Text is scaled down to the min scale and scrolls if it still does not fit. */
        WHEN_NEEDED,
        /** Text always scrolls (ticker), even if it fits. */
        ALWAYS,
        /** Not a scroll mode: text is scaled down to the min scale and may overflow the label up to the {@link BERLabel#clippingArea}. */
        FLEX_FIT
    }

    public static final float GLYPH_HEIGHT = 8F;
    public static final float AUTO_SCROLL_GAP = -1F;

    private static final float EPSILON = 0.0001F;
    private static final float BAKED_GLYPH_Y_OFFSET = 3F;
    private static final int MAX_SCROLL_COPIES = 64;
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final ClippingBufferSource CLIPPING_BUFFER_SOURCE = new ClippingBufferSource();

    public final Property<Rectangle> clippingArea = new Property<>(Rectangle.withSize(-Integer.MAX_VALUE / 2, -Integer.MAX_VALUE / 2, Integer.MAX_VALUE, Integer.MAX_VALUE));
    public final NumberProperty<Float> x = new NumberProperty<>(0F);
    public final NumberProperty<Float> y = new NumberProperty<>(0F);
    public final VirtualProperty<Point> position = new VirtualProperty<>(Point.of(0, 0),
        () -> Point.of(x.get(), y.get()),
        (p) -> {
            x.set((float)p.x());
            y.set((float)p.y());
        });
    public final Property<ETextAlignment> horizontalAlign = new Property<>(ETextAlignment.LEFT);
    public final Property<EVerticalAlignment> verticalAlign = new Property<>(EVerticalAlignment.TOP);
    public final Property<Component> text = new Property<>(TextUtils.empty());
    public final ColorProperty color = new ColorProperty(DLColor.WHITE, DLColor.WHITE);
    public final ColorProperty backgroundColor = new ColorProperty(DLColor.TRANSPARENT, DLColor.TRANSPARENT);
    public final Property<PaddingF> backgroundPadding = new Property<>(new PaddingF(1, 1, 1, 1));
    public final BooleanProperty fullBackground = new BooleanProperty(false);
    public final BooleanProperty glowing = new BooleanProperty(false);
    public final BooleanProperty shadow = new BooleanProperty(false);
    public final NumberProperty<Float> lineHeight = new NumberProperty<>(GLYPH_HEIGHT, 1F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> preferredWidth = new NumberProperty<>(16F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> preferredHeight = new NumberProperty<>(16F, 0F, (float) Integer.MAX_VALUE);
    public final VirtualProperty<Size> preferredSize = new VirtualProperty<>(Size.of(0F, 0F),
        () -> Size.of(preferredWidth.get(), preferredHeight.get()),
        (p) -> {
            preferredWidth.set((float)p.w());
            preferredHeight.set((float)p.h());
        });
    public final VirtualProperty<Rectangle> layoutArea = new VirtualProperty<Rectangle>(Rectangle.withSize(0, 0, 16, 16),
        () -> Rectangle.withSize(x.get(), y.get(), preferredWidth.get(), preferredHeight.get()),
        (r) -> {
            x.set((float)r.x());
            y.set((float)r.y());
            preferredWidth.set((float)r.width());
            preferredHeight.set((float)r.height());
        });
    public final NumberProperty<Float> horizontalMinScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> horizontalMaxScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final VirtualProperty<Pair<Float, Float>> horizontalScale = new VirtualProperty<>(Pair.of(0F, 0F),
        () -> Pair.of(horizontalMinScale.get(), horizontalMaxScale.get()),
        (p) -> {
            horizontalMinScale.set(p.getFirst());
            horizontalMaxScale.set(p.getSecond());
        });
    public final NumberProperty<Float> verticalMinScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final NumberProperty<Float> verticalMaxScale = new NumberProperty<>(1F, 0F, (float) Integer.MAX_VALUE);
    public final VirtualProperty<Pair<Float, Float>> verticalScale = new VirtualProperty<>(Pair.of(0F, 0F),
        () -> Pair.of(verticalMinScale.get(), verticalMaxScale.get()),
        (p) -> {
            verticalMinScale.set(p.getFirst());
            verticalMaxScale.set(p.getSecond());
        });
    public final BooleanProperty keepAspectRatio = new BooleanProperty(false);
    public final NumberProperty<Float> horizontalScrollingSpeed = new NumberProperty<>(4F);
    public final NumberProperty<Float> verticalScrollingSpeed = new NumberProperty<>(4F);
    public final Property<EScrollMode> horizontalScrollMode = new Property<>(EScrollMode.NEVER);
    public final Property<EScrollMode> verticalScrollMode = new Property<>(EScrollMode.NEVER);
    public final NumberProperty<Float> scrollGap = new NumberProperty<>(AUTO_SCROLL_GAP);
    public final NumberProperty<Float> scrollPause = new NumberProperty<>(0F, 0F, (float) Integer.MAX_VALUE);
    public final BooleanProperty scrollPingPong = new BooleanProperty(false);


    private int[] cachedCodePoints = new int[0];
    private Style[] cachedStyles = new Style[0];
    private float[] cachedPrefixWidths = new float[] { 0f };
    private float cachedUnscaledTextWidth = 0f;
    private float cachedGlyphTop = 0f;
    private float cachedGlyphBottom = GLYPH_HEIGHT;
    private boolean textDirty = true;
    private FontSet lastFontSet;

    private int visibleFrom;
    private int visibleTo;
    private final FormattedCharSequence visibleSequence = sink -> {
        for (int i = visibleFrom; i <= visibleTo; i++) {
            if (!sink.accept(i, cachedStyles[i], cachedCodePoints[i])) {
                return false;
            }
        }
        return true;
    };

    private boolean layoutValid;
    private float cachedFinalHScale = 1f;
    private float cachedFinalVScale = 1f;
    private float contentWidth;
    private float contentHeight;
    private boolean hScrollActive;
    private boolean vScrollActive;
    private float clipLeft;
    private float clipTop;
    private float clipRight;
    private float clipBottom;
    private float textClipTop;
    private float textClipBottom;
    private float baseX;
    private float baseY;

    public BERLabel() {
        text.addChangeListener((o, n) -> {
            if (!Objects.equals(o, n)) {
                textDirty = true;
            }
        });
    }

    public void invalidate() {
        textDirty = true;
    }

    private void updateTextCache() {
        Font font = Minecraft.getInstance().font;
        FontSet fontSet = ((FontAccessor)font).dragonlib$invokeGetFontSet(Style.DEFAULT_FONT);
        if (fontSet != lastFontSet) {
            lastFontSet = fontSet;
            textDirty = true;
        }
        if (!textDirty) return;
        textDirty = false;

        Component component = text.get();
        if (component == null) {
            cachedCodePoints = new int[0];
            cachedStyles = new Style[0];
            cachedPrefixWidths = new float[] { 0f };
            cachedUnscaledTextWidth = 0f;
            cachedGlyphTop = 0f;
            cachedGlyphBottom = GLYPH_HEIGHT;
            return;
        }

        IntArrayList codePoints = new IntArrayList();
        List<Style> styles = new ArrayList<>();
        Language.getInstance().getVisualOrder(component).accept((index, style, codePoint) -> {
            codePoints.add(codePoint);
            styles.add(style);
            return true;
        });

        StringSplitter.WidthProvider widthProvider = ((StringSplitterAccessor)font.getSplitter()).dragonlib$getWidthProvider();
        int n = codePoints.size();
        int[] cps = codePoints.toIntArray();
        Style[] stl = styles.toArray(new Style[n]);
        float[] prefix = new float[n + 1];
        float glyphTop = 0f;
        float glyphBottom = GLYPH_HEIGHT;
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + widthProvider.getWidth(cps[i], stl[i]);
            BakedGlyph glyph = ((FontAccessor)font).dragonlib$invokeGetFontSet(stl[i].getFont()).getGlyph(cps[i]);
            glyphTop = Math.min(glyphTop, glyph.up - BAKED_GLYPH_Y_OFFSET);
            glyphBottom = Math.max(glyphBottom, glyph.down - BAKED_GLYPH_Y_OFFSET);
        }
        cachedGlyphTop = glyphTop;
        cachedGlyphBottom = glyphBottom;

        cachedCodePoints = cps;
        cachedStyles = stl;
        cachedPrefixWidths = prefix;
        cachedUnscaledTextWidth = prefix[n];
    }

    private int firstCharEndingAfter(float x) {
        int lo = 0, hi = cachedCodePoints.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (cachedPrefixWidths[mid + 1] > x) hi = mid; else lo = mid + 1;
        }
        return lo;
    }

    private int lastCharStartingBefore(float x) {
        int lo = 0, hi = cachedCodePoints.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (cachedPrefixWidths[mid] < x) lo = mid + 1; else hi = mid;
        }
        return lo - 1;
    }

    private void updateLayout() {
        float targetW = preferredWidth.get();
        float targetH = preferredHeight.get();
        float lineH = lineHeight.get();
        float textW = cachedUnscaledTextWidth;

        float minHScale = Math.max(horizontalMinScale.get(), EPSILON);
        float maxHScale = horizontalMaxScale.get() <= 0 ? Float.MAX_VALUE : horizontalMaxScale.get();
        float minVScale = Math.max(verticalMinScale.get(), EPSILON);
        float maxVScale = verticalMaxScale.get() <= 0 ? Float.MAX_VALUE : verticalMaxScale.get();

        EScrollMode hMode = horizontalScrollMode.get();
        EScrollMode vMode = verticalScrollMode.get();
        boolean hCanScroll = horizontalScrollingSpeed.get() != 0.0F;
        boolean vCanScroll = verticalScrollingSpeed.get() != 0.0F;
        boolean hAlways = hMode == EScrollMode.ALWAYS && hCanScroll;
        boolean vAlways = vMode == EScrollMode.ALWAYS && vCanScroll;

        float idealH = textW > 0 ? targetW / textW : 1.0F;
        float idealV = targetH / lineH;

        float hScale, vScale;
        if (keepAspectRatio.get()) {
            float lo = Math.max(minHScale, minVScale);
            float hi = Math.max(lo, Math.min(maxHScale, maxVScale));
            float ideal = Math.min(hAlways ? Float.MAX_VALUE : idealH, vAlways ? Float.MAX_VALUE : idealV);
            hScale = vScale = Mth.clamp(ideal == Float.MAX_VALUE ? 1.0F : ideal, lo, hi);
        } else {
            hScale = Mth.clamp(hAlways ? 1.0F : idealH, minHScale, maxHScale);
            vScale = Mth.clamp(vAlways ? 1.0F : idealV, minVScale, maxVScale);
        }

        contentWidth = textW * hScale;
        contentHeight = lineH * vScale;
        boolean hOverflow = contentWidth > targetW + EPSILON;
        boolean vOverflow = contentHeight > targetH + EPSILON;

        hScrollActive = hAlways || (hMode == EScrollMode.WHEN_NEEDED && hCanScroll && hOverflow);
        vScrollActive = vAlways || (vMode == EScrollMode.WHEN_NEEDED && vCanScroll && vOverflow);
        cachedFinalHScale = hScale;
        cachedFinalVScale = vScale;

        float textX = x.get();
        float textY = y.get();
        Rectangle area = clippingArea.get();
        clipLeft = (float)Math.max(area.left(), textX);
        clipRight = (float)Math.min(area.right(), textX + targetW);
        clipTop = (float)Math.max(area.top(), textY);
        clipBottom = (float)Math.min(area.bottom(), textY + targetH);
        if (hMode == EScrollMode.FLEX_FIT && hOverflow) {
            clipLeft = (float)area.left();
            clipRight = (float)area.right();
        }
        if (vMode == EScrollMode.FLEX_FIT && vOverflow) {
            clipTop = (float)area.top();
            clipBottom = (float)area.bottom();
        }

        textClipTop = clipTop;
        textClipBottom = clipBottom;
        if (!vScrollActive) {
            float glyphOffsetY = (lineH - GLYPH_HEIGHT) / 2.0F;
            float overTop = Math.max(0.0F, -(glyphOffsetY + cachedGlyphTop)) * vScale;
            float overBottom = Math.max(0.0F, glyphOffsetY + cachedGlyphBottom + (shadow.get() ? 1.0F : 0.0F) - lineH) * vScale;
            textClipTop = (float)Math.max(area.top(), clipTop - overTop);
            textClipBottom = (float)Math.min(area.bottom(), clipBottom + overBottom);
        }

        baseX = textX + switch (horizontalAlign.get()) {
            case CENTER -> (targetW - contentWidth) / 2.0F;
            case RIGHT -> targetW - contentWidth;
            default -> 0.0F;
        };
        baseY = textY + verticalAlign.get().offset(targetH, contentHeight);

        layoutValid = targetW > 0.0F && targetH > 0.0F && clipRight > clipLeft && clipBottom > clipTop;
    }

    private static double currentTimeSeconds() {
        return Util.getMillis() / 1000.0D;
    }

    private static float loopPosition(double time, float speed, float viewport, float content, float period, float pause) {
        float absSpeed = Math.abs(speed);
        float restPosition = speed > 0 ? 0.0F : viewport - content;
        double travelTime = period / absSpeed;
        double t = mod(time, travelTime + pause);
        double moved = Math.max(0.0D, t - pause) * absSpeed;
        double offset = mod((viewport - restPosition) + Math.signum(speed) * moved, period);
        return (float)(viewport - offset);
    }

    private static float pingPongPosition(double time, float speed, float viewport, float content, float pause) {
        float a = 0.0F;
        float b = viewport - content;
        float distance = Math.abs(b - a);
        if (distance < EPSILON) {
            return a;
        }
        float from = speed > 0 ? a : b;
        float to = speed > 0 ? b : a;
        double travelTime = distance / Math.abs(speed);
        double t = mod(time, 2.0D * (pause + travelTime));
        if (t < pause) return from;
        t -= pause;
        if (t < travelTime) return (float)(from + (to - from) * (t / travelTime));
        t -= travelTime;
        if (t < pause) return to;
        t -= pause;
        return (float)(to + (from - to) * (t / travelTime));
    }

    private static double mod(double value, double m) {
        double r = value % m;
        return r < 0 ? r + m : r;
    }

    public void render(DLGraphics graphics) {
        render(graphics, glowing.get() ? LightTexture.FULL_BRIGHT : graphics.packedLight());
    }

    public void render(DLGraphics graphics, int light) {
        updateTextCache();
        int charCount = cachedCodePoints.length;
        if (charCount == 0) return;

        updateLayout();
        if (!layoutValid) return;

        float hScale = cachedFinalHScale;
        float vScale = cachedFinalVScale;
        float viewportW = clipRight - clipLeft;
        float viewportH = clipBottom - clipTop;
        double time = (hScrollActive || vScrollActive) ? currentTimeSeconds() : 0;
        float pause = scrollPause.get() / 20.0F;
        boolean pingPong = scrollPingPong.get();
        float gap = scrollGap.get();

        float startX = baseX, periodX = 0;
        if (hScrollActive) {
            float speed = horizontalScrollingSpeed.get();
            if (pingPong) {
                startX = clipLeft + pingPongPosition(time, speed, viewportW, contentWidth, pause);
            } else {
                periodX = contentWidth + (gap < 0 ? viewportW : gap);
                startX = clipLeft + loopPosition(time, speed, viewportW, contentWidth, periodX, pause);
            }
        }
        float startY = baseY, periodY = 0;
        if (vScrollActive) {
            float speed = verticalScrollingSpeed.get();
            if (pingPong) {
                startY = clipTop + pingPongPosition(time, speed, viewportH, contentHeight, pause);
            } else {
                periodY = contentHeight + (gap < 0 ? viewportH : gap);
                startY = clipTop + loopPosition(time, speed, viewportH, contentHeight, periodY, pause);
            }
        }
        if (periodX > 0) {
            startX = firstVisibleCopy(startX, periodX, contentWidth, clipLeft);
        }
        if (periodY > 0) {
            startY = firstVisibleCopy(startY, periodY, contentHeight, clipTop);
        }

        DLColor bgColor = backgroundColor.get();
        boolean hasBg = bgColor != null && bgColor.getAlphaF() > 0;
        PaddingF padding = hasBg ? backgroundPadding.get() : null;
        if (hasBg && fullBackground.get()) {
            renderFullBackground(graphics, bgColor, padding, light);
        }

        int argb = color.get().getAsARGB();
        int alpha = argb >>> 24;
        boolean hasText = alpha > 0;
        if (alpha > 0 && alpha < 4) {
            argb = (argb & 0x00FFFFFF) | (4 << 24);
        }

        boolean smoothText = SmoothTextRenderTypes.isEnabled();
        boolean dropShadow = shadow.get();
        float glyphOffsetY = (lineHeight.get() - GLYPH_HEIGHT) / 2.0F;

        boolean deferText = smoothText && SmoothTextDeferred.shouldDefer(graphics);
        MultiBufferSource.BufferSource flushSource = smoothText && !deferText ? smoothTextFlushSource(graphics) : null;
        textMode = !smoothText ? TextMode.VANILLA : deferText ? TextMode.DEFERRED : TextMode.SMOOTH;
        if (flushSource != null) {
            flushSource.endBatch();
        }

        int copies = 0;
        for (float posX = startX; ; posX += periodX) {
            for (float posY = startY; ; posY += periodY) {
                if (hasBg && !fullBackground.get()) {
                    renderTextBackground(graphics, posX, posY, bgColor, padding, light);
                    if (flushSource != null) {
                        flushSource.endBatch();
                    }
                }
                if (hasText) {
                    renderText(graphics, posX, posY, hScale, vScale, glyphOffsetY, argb, dropShadow, light);
                }
                if (periodY <= 0 || posY + periodY >= clipBottom || ++copies >= MAX_SCROLL_COPIES) break;
            }
            if (periodX <= 0 || posX + periodX >= clipRight || ++copies >= MAX_SCROLL_COPIES) break;
        }

        if (flushSource != null) {
            flushSource.endBatch();
        }
    }

    private static boolean unsupportedSourceLogged = false;

    private static MultiBufferSource.BufferSource smoothTextFlushSource(DLGraphics graphics) {
        if (graphics.multiBufferSource() instanceof MultiBufferSource.BufferSource bufferSource) {
            return bufferSource;
        }
        if (!unsupportedSourceLogged) {
            unsupportedSourceLogged = true;
            DragonLib.LOGGER.info("Smooth text: buffer source {} cannot be flushed, the draw order of labels rendered through it is not guaranteed.", graphics.multiBufferSource().getClass().getName());
        }
        return null;
    }

    private static float firstVisibleCopy(float start, float period, float content, float viewportStart) {
        double k = Math.floor((viewportStart - (start + content)) / period) + 1;
        return (float)(start + k * period);
    }

    private void renderText(DLGraphics graphics, float posX, float posY, float hScale, float vScale, float glyphOffsetY, int argb, boolean dropShadow, int light) {
        float localClipLeft = (clipLeft - posX) / hScale;
        float localClipRight = (clipRight - posX) / hScale;
        float localClipTop = (textClipTop - posY) / vScale;
        float localClipBottom = (textClipBottom - posY) / vScale;
        int charCount = cachedCodePoints.length;
        int from = Math.max(0, firstCharEndingAfter(localClipLeft) - 1);
        int to = Math.min(charCount - 1, lastCharStartingBefore(localClipRight) + 1);

        if (from > to) return;

        PoseStack poseStack = graphics.poseStack();
        poseStack.pushPose();
        poseStack.translate(posX, posY, 0);
        poseStack.scale(hScale, vScale, 1.0f);
        PoseStack.Pose pose = poseStack.last();

        CLIPPING_BUFFER_SOURCE.setup(graphics.multiBufferSource(), pose.pose(), pose.normal(), localClipLeft, localClipTop, localClipRight, localClipBottom);
        visibleFrom = from;
        visibleTo = to;

        switch (textMode) {
            case VANILLA -> {
                CLIPPING_BUFFER_SOURCE.setRenderTypeMapper(null);
                drawVisibleText(glyphOffsetY, argb, dropShadow, light, from);
            }
            case SMOOTH -> {
                CLIPPING_BUFFER_SOURCE.setRenderTypeMapper(SmoothTextRenderTypes::getFor);
                drawVisibleText(glyphOffsetY, argb, dropShadow, light, from);
            }
            case DEFERRED -> {
                CLIPPING_BUFFER_SOURCE.setup(SmoothTextDeferred.source(), pose.pose(), pose.normal(), localClipLeft, localClipTop, localClipRight, localClipBottom);
                CLIPPING_BUFFER_SOURCE.setRenderTypeMapper(SmoothTextRenderTypes::getFor);
                drawVisibleText(glyphOffsetY, argb, dropShadow, light, from);
            }
        }
        CLIPPING_BUFFER_SOURCE.flush();
        CLIPPING_BUFFER_SOURCE.setRenderTypeMapper(null);

        poseStack.popPose();
    }

    private enum TextMode { VANILLA, SMOOTH, DEFERRED }

    private TextMode textMode = TextMode.VANILLA;

    private void drawVisibleText(float glyphOffsetY, int argb, boolean dropShadow, int light, int from) {
        Minecraft.getInstance().font.drawInBatch(
            visibleSequence,
            cachedPrefixWidths[from], glyphOffsetY,
            argb,
            dropShadow,
            IDENTITY,
            CLIPPING_BUFFER_SOURCE,
            Font.DisplayMode.POLYGON_OFFSET,
            0,
            light
        );
    }

    private void renderFullBackground(DLGraphics graphics, DLColor bgColor, PaddingF padding, int light) {
        float bgX = x.get();
        float bgY = y.get();
        float bgRight = bgX + preferredWidth.get();
        float bgBottom = bgY + preferredHeight.get();

        if (horizontalScrollMode.get() == EScrollMode.FLEX_FIT && !hScrollActive) {
            bgX = Math.min(bgX, baseX);
            bgRight = Math.max(bgRight, baseX + contentWidth);
        }
        if (verticalScrollMode.get() == EScrollMode.FLEX_FIT && !vScrollActive) {
            bgY = Math.min(bgY, baseY);
            bgBottom = Math.max(bgBottom, baseY + contentHeight);
        }
        fillClipped(graphics, bgColor, light, bgX - padding.left(), bgY - padding.top(), bgRight + padding.right(), bgBottom + padding.bottom(), clipLeft - padding.left(), clipTop - padding.top(), clipRight + padding.right(), clipBottom + padding.bottom());
    }

    private void renderTextBackground(DLGraphics graphics, float posX, float posY, DLColor bgColor, PaddingF padding, int light) {
        float right = posX + contentWidth;
        float bottom = posY + contentHeight;
        fillClipped(graphics, bgColor, light, posX - padding.left(), posY - padding.top(), right + padding.right(), bottom + padding.bottom(), posX >= clipLeft ? clipLeft - padding.left() : clipLeft, posY >= clipTop ? clipTop - padding.top() : clipTop, right <= clipRight ? clipRight + padding.right() : clipRight, bottom <= clipBottom ? clipBottom + padding.bottom() : clipBottom);
    }

    private void fillClipped(DLGraphics graphics, DLColor bgColor, int light, float left, float top, float right, float bottom, float boundLeft, float boundTop, float boundRight, float boundBottom) {
        Rectangle area = clippingArea.get();
        float visibleLeft = Math.max(left, Math.max(boundLeft, (float)area.left()));
        float visibleTop = Math.max(top, Math.max(boundTop, (float)area.top()));
        float visibleRight = Math.min(right, Math.min(boundRight, (float)area.right()));
        float visibleBottom = Math.min(bottom, Math.min(boundBottom, (float)area.bottom()));
        if (visibleRight > visibleLeft && visibleBottom > visibleTop) {
            RenderUtils.fillColor(graphics, new Vector3f(visibleLeft, visibleTop, 0.0f), visibleRight - visibleLeft, visibleBottom - visibleTop, bgColor, Direction.NORTH, light, false);
        }
    }

    public float getRenderedWidth() {
        updateTextCache();
        updateLayout();
        return this.cachedUnscaledTextWidth * this.cachedFinalHScale;
    }

    public float getRenderedHeight() {
        updateTextCache();
        updateLayout();
        return lineHeight.get() * this.cachedFinalVScale;
    }
}
