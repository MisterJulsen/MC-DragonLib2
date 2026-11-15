package de.mrjulsen.mcdragonlib.client.ber;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.PaddingF;
import de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource;
import de.mrjulsen.mcdragonlib.client.util.DLGraphics;
import de.mrjulsen.mcdragonlib.client.util.RenderUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;

public class BERLabel {

    public enum EScrollMode { NEVER, WHEN_NEEDED, ALWAYS, FLEX_FIT }

    private static final Matrix4f IDENTITY = new Matrix4f();

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

    private final ClippingBufferSource clippingBufferSource = new ClippingBufferSource();

    private long lastRenderTime = 0;
    private float horizontalScrollOffset = 0.0f;
    private float verticalScrollOffset = 0.0f;

    private static final int[] NO_CODE_POINTS = new int[0];
    private static final Style[] NO_STYLES = new Style[0];
    private static final float[] NO_WIDTHS = new float[] { 0f };

    private int[] cachedCodePoints = NO_CODE_POINTS;
    private Style[] cachedStyles = NO_STYLES;
    private float[] cachedPrefixWidths = NO_WIDTHS;
    private float cachedUnscaledTextWidth = 0f;
    private int visibleFrom, visibleTo;
    private final FormattedCharSequence visibleSequence = sink -> {
        for (int i = visibleFrom; i <= visibleTo; i++) {
            if (!sink.accept(i, cachedStyles[i], cachedCodePoints[i])) {
                return false;
            }
        }
        return true;
    };
    private Component lastRenderedText = null;
    
    private float cachedFinalHScale = 1.0f;
    private float cachedFinalVScale = 1.0f;

    public BERLabel() {
        text.withAfterPropertyChangedCallback((oldVal, newVal) -> invalidateCache());
    }

    private void invalidateCache() {
        cachedCodePoints = NO_CODE_POINTS;
        cachedStyles = NO_STYLES;
        cachedPrefixWidths = NO_WIDTHS;
        cachedUnscaledTextWidth = 0f;
        lastRenderedText = null;
    }

    private void updateGlyphCache(Component component) {
        if (component == null || component.equals(lastRenderedText)) return;

        StringSplitter.WidthProvider widthProvider = ((StringSplitterAccessor)Minecraft.getInstance().font.getSplitter()).dragonlib$getWidthProvider();
        List<Style> styles = new ArrayList<>();
        List<Integer> codePoints = new ArrayList<>();
        StringDecomposer.iterateFormatted(component, Style.EMPTY, (charIndex, style, codePoint) -> {
            codePoints.add(codePoint);
            styles.add(style);
            return true;
        });

        int n = codePoints.size();
        int[] cps = new int[n];
        Style[] stl = styles.toArray(new Style[n]);
        float[] prefix = new float[n + 1];
        for (int i = 0; i < n; i++) {
            cps[i] = codePoints.get(i);
            prefix[i + 1] = prefix[i] + widthProvider.getWidth(cps[i], stl[i]);
        }

        cachedCodePoints = cps;
        cachedStyles = stl;
        cachedPrefixWidths = prefix;
        cachedUnscaledTextWidth = prefix[n];
        lastRenderedText = component;
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
        boolean hasBg = bgColor != null && bgColor.getAlphaF() > 0;
        if (hasBg) {
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

        int charCount = cachedCodePoints.length;
        if (charCount > 0) {
            float localClipLeft = (float)((finalClipBounds.left() - finalX) / finalHScale);
            float localClipRight = (float)((finalClipBounds.right() - finalX) / finalHScale);
            float localClipTop = (float)((finalClipBounds.top() - finalY) / finalVScale);
            float localClipBottom = (float)((finalClipBounds.bottom() - finalY) / finalVScale);

            int from = Math.max(0, firstCharEndingAfter(localClipLeft) - 1);
            int to = Math.min(charCount - 1, lastCharStartingBefore(localClipRight) + 1);

            if (from <= to) {
                PoseStack poseStack = graphics.poseStack();
                poseStack.pushPose();
                poseStack.translate(finalX, finalY, hasBg ? 0.022 : 0.002);
                poseStack.scale(finalHScale, finalVScale, 1.0f);
                PoseStack.Pose pose = poseStack.last();

                clippingBufferSource.setup(graphics.multiBufferSource(), pose.pose(), pose.normal(), localClipLeft, localClipTop, localClipRight, localClipBottom);
                visibleFrom = from;
                visibleTo = to;

                Minecraft.getInstance().font.drawInBatch(
                    visibleSequence,
                    cachedPrefixWidths[from], 0,
                    color.get().getAsARGB(),
                    false,
                    IDENTITY,
                    clippingBufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    light
                );

                poseStack.popPose();
            }
        }

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
        updateGlyphCache(this.text.get());
        calculateFinalScales();
        
        return this.cachedUnscaledTextWidth * this.cachedFinalHScale;
    }

    public float getRenderedHeight() {
        updateGlyphCache(this.text.get());
        calculateFinalScales();
        
        return 8.0f * this.cachedFinalVScale;
    }
}