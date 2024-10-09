package de.mrjulsen.mcdragonlib.client.ber;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.util.BERUtils;
import de.mrjulsen.mcdragonlib.client.util.FontUtils;
import de.mrjulsen.mcdragonlib.data.Cache;
import de.mrjulsen.mcdragonlib.mixin.BakedGlyphAccessor;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;

/**
 * A simple component to display text on BERs with some customization options
 * (e.g. scrolling text, scaling, cropping, etc.). Make sure to call the {@code renderTick()}
 * method ONCE before rendering to enable smooth scrolling. Do not call it in {@code tick()},
 * otherwise the text would only have 20 fps, and do not call it multiple times
 * (otherwise the scrolling speed will be faster)
 */
public class BERLabel {

    protected static record TextDataCache(float textWidth, float scaledTextWidth, float scale, boolean shouldScroll) {}
    protected static record CharData(int charCode, GlyphInfo glyphInfo, float glyphWidth, BakedGlyphAccessor glyph, float glyphUVDiff) {}

    public static enum BoundsHitReaction {
        /** Ignores the given limit and writes beyond the limits, but with minimal scaling to exceed the limit as little as possible. */
        IGNORE,
        /** Crops text at borders and uses the minimum scaling to keep as much text readable as possible.*/
        CUT_OFF,
        /** Allows the text to scroll if it no longer fits the bounds even with minimal scaling. The scrolling text then has normal scaling again. */
        SCALE_SCROLL,
        /** Allows the text to scroll if it no longer fits the bounds with default scaling. */
        SCROLL;
    }

    private static final byte CHAR_SIZE = 8;
    public static final float INVALID = -1;
    public static final float DEFAULT_SCROLL_SPEED = 0.5f;
    public static final BoundsHitReaction DEFAULT_BOUNDS_HIT_REACTION = BoundsHitReaction.CUT_OFF;

    private final FontUtils fontUtils;
    private Component text;

    private float x = 0;
    private float y = 0;
    private boolean widthLimited = false;
    private float maxWidth = 0;
    private BoundsHitReaction boundsHitReaction = BoundsHitReaction.CUT_OFF;
    private float minScale = 1;
    private float scale = 1;
    private float yScale = 1;
    private float scrollingSpeed = DEFAULT_SCROLL_SPEED;
    private boolean forceScrolling = false;
    private boolean center = false;
    private int color = 0xFFFFFFFF;
    private int backgroundColor = 0;
    private boolean backgroundColorFullLabel = false;

    // Caching 
    private final Cache<TextDataCache> textData = new Cache<>(this::calc);
    private final Cache<Float> scaledTextWidth = new Cache<>(() -> {
        float textWidth = getFontUtils().font.width(getText());
        float rawXScale = textWidth * getScale();
        return rawXScale;
    });
    private final Cache<Float> textWidth = new Cache<>(() -> {
        return getFontUtils().font.width(getText()) * textData.get().scale();
    });
    private final Map<Integer, CharData> charDataCache = new HashMap<>();

    // Mem
    private float xScrollOffset = 0;
    
    public BERLabel() {
        this(TextUtils.empty());
    }

    public BERLabel(Component initialText) {
        this(new FontUtils(Style.DEFAULT_FONT), initialText);
    }

    public BERLabel(FontUtils fontUtils, Component initialText) {
        this.fontUtils = fontUtils;
        this.text = initialText;
    }

    /**
     * The position of the label.
     * @param x x coordinate
     * @param y y coordinate
     * @return this
     */
    public BERLabel setPos(float x, float y) {
        this.x = x;
        this.y = y;
        resetCaches();
        return this;
    }

    /**
     * Defines the maximum width the text can have.
     * @param maxWidth The max width.
     * @param reaction What happens when the limit is reached or exceeded.
     * @return this
     */
    public BERLabel setMaxWidth(float maxWidth, BoundsHitReaction reaction) {
        this.maxWidth = maxWidth;
        this.boundsHitReaction = reaction;
        this.widthLimited = true;
        resetCaches();
        return this;
    }

    /**
     * Removes the maximum width of this label.
     * @return this
     */
    public BERLabel noMaxWidth() {
        this.maxWidth = 0;
        this.widthLimited = false;
        resetCaches();
        return this;
    }

    /**
     * The scaling of the text. {@code def} specifies the normal scaling, {@code min} the minimum scaling if there is not enough space.
     * @param def Default scaling
     * @param min Minimum scaling
     * @return this
     */
    public BERLabel setScale(float def, float min) {
        this.minScale = min;
        this.scale = def;
        resetCaches();
        return this;
    }

    /**
     * How fast the text should scroll.
     * @param speed Per tick speed value
     * @return this
     */
    public BERLabel setScrollingSpeed(float speed) {
        this.scrollingSpeed = speed;
        resetCaches();
        return this;
    }

    /**
     * Forces the text to scroll all the time.
     * @param b
     * @return this
     */
    public BERLabel setForceScrolling(boolean b) {
        this.forceScrolling = b;
        resetCaches();
        return this;
    }
    
    /**
     * The y scale of the text.
     * @param scale the scale value
     * @return this
     */
    public BERLabel setYScale(float scale) {
        this.yScale = scale;
        resetCaches();
        return this;
    }

    /**
     * Places the text at the center of the label defined by the position and the max width. This won't work when there is no max width.
     * @param b
     * @return this
     */
    public BERLabel setCentered(boolean b) {
        this.center = b;
        resetCaches();
        return this;
    }

    /**
     * The background color for the text of this label.
     * @param color The color
     * @param fullSize whether the full label should use this background color or only the text.
     * @return this
     */
    public BERLabel setBackground(int color, boolean fullSize) {
        this.backgroundColor = color;
        this.backgroundColorFullLabel = fullSize;
        return this;
    }

    /**
     * Set the font color.
     * @param color The color.
     * @return this
     */
    public BERLabel setColor(int color) {
        this.color = color;
        return this;
    }
    
    /**
     * Change the text of the label.
     * @param text The new text.
     * @return this
     */
    public BERLabel setText(Component text) {
        this.text = text;
        resetCaches();
        return this;
    }

    public FontUtils getFontUtils() {
        return fontUtils;
    }

    public Component getText() {
        return text;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public BoundsHitReaction getBoundsHitReaction() {
        return boundsHitReaction;
    }

    public float getMinScale() {
        return minScale;
    }

    public float getScale() {
        return scale;
    }

    public float getScrollingSpeed() {
        return scrollingSpeed;
    }

    public boolean isForceScrolling() {
        return forceScrolling;
    }

    public boolean isCentered() {
        return center;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isBackgroundColorFullSize() {
        return backgroundColorFullLabel;
    }

    public int getColor() {
        return color;
    }

    public float getYScale() {
        return yScale;
    }

    public float getTextWidth() {
        return textWidth.get();
    }

    private void resetCaches() {
        textData.clear();
        charDataCache.clear();
        scaledTextWidth.clear();
        textWidth.clear();
    }

    protected TextDataCache calc() {
        float textWidth = getFontUtils().font.width(getText());
        if (!widthLimited) {
            return new TextDataCache(textWidth, textWidth, getScale(), isForceScrolling());
        }

        float rawXScale = getMaxWidth() / textWidth;
        float minScale = (getBoundsHitReaction() == BoundsHitReaction.SCROLL ? getScale() : getMinScale());
        float defScale = getScale();
        float finalXScale = MathUtils.clamp(rawXScale, minScale, defScale);
        boolean mustScroll = isForceScrolling() || (rawXScale < getMinScale() && getMaxWidth() > 0 && (getBoundsHitReaction() == BoundsHitReaction.SCROLL || getBoundsHitReaction() == BoundsHitReaction.SCALE_SCROLL));

        if (mustScroll) { // Reset the scale if it has to scroll
            finalXScale = defScale;
        }

        return new TextDataCache(textWidth, textWidth * finalXScale, finalXScale, mustScroll);
    }

    /** Should be called once every frame for a smooth scrolling experience. */
    public void renderTick() {
        if (textData.get().shouldScroll()) {
            float scaledMaxWidth = (!widthLimited ? scaledTextWidth.get() : getMaxWidth()) / textData.get().scale();
            xScrollOffset -= Minecraft.getInstance().getDeltaFrameTime() * getScrollingSpeed();
            if (xScrollOffset < -textData.get().textWidth()) {
                xScrollOffset = scaledMaxWidth;
            }
        }
    }

    public void render(BERGraphics<?> graphics) {
        render(graphics, graphics.packedLight());
    }

    @SuppressWarnings("resource")
    public void render(BERGraphics<?> graphics, int light) {

        getFontUtils().reset();
        float scaledMaxWidth = (!widthLimited ? scaledTextWidth.get() : getMaxWidth()) / textData.get().scale();
        graphics.poseStack().pushPose(); {
            graphics.poseStack().translate(getX(), getY(), 0);

            graphics.poseStack().pushPose(); {
                graphics.poseStack().scale(textData.get().scale(), getYScale(), 1);
                float txtX = textData.get().shouldScroll() ? xScrollOffset : (center ? Math.max(0, scaledMaxWidth / 2f - textData.get().textWidth() / 2f) : 0);

                if (getBackgroundColor() != 0 && !getText().getString().isEmpty()) {
                    if (isBackgroundColorFullSize()) {
                        BERUtils.fillColor(graphics, -1, -1, 0, scaledMaxWidth + 2, Minecraft.getInstance().font.lineHeight + 1, getBackgroundColor(), Direction.NORTH, light);
                    } else {
                        BERUtils.fillColor(graphics, (center ? Math.max(0, scaledMaxWidth / 2f - textData.get().textWidth() / 2f) : 0) - 1, -1, 0, Math.min(scaledTextWidth.get() / textData.get().scale(), scaledMaxWidth) + 2, Minecraft.getInstance().font.lineHeight + 1, getBackgroundColor(), Direction.NORTH, light);
                    }
                    graphics.poseStack().translate(0, 0, 0.01f);
                }

                renderTextInBounds(graphics.poseStack(), getFontUtils(), graphics.multiBufferSource(), getText(), light,
                    txtX,
                    0,
                    getBoundsHitReaction() == BoundsHitReaction.IGNORE ? Integer.MAX_VALUE : scaledMaxWidth
                );
            }
            graphics.poseStack().popPose();
        }
        graphics.poseStack().popPose();
    }


    protected void renderTextInBounds(PoseStack poseStack, FontUtils fontUtils, MultiBufferSource bufferSource, Component text, int packedLight, float xOffset, float xLeft, float xRight) {
        if (xRight <= xLeft) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(xLeft + (xOffset > 0 ? xOffset : 0), 0, 0);
        Font.StringRenderOutput sro = fontUtils.font.new StringRenderOutput(bufferSource, 0, 0, getColor(), false, poseStack.last().pose(), Font.DisplayMode.NORMAL, packedLight);
        
        float newX = xOffset;
        float glyphTranslation = 0;
        final float charSize = CHAR_SIZE + (text.getStyle().isBold() ? 1 : 0);

        for (int i = 0; i < text.getString().length(); i++) {
            int charCode = text.getString().charAt(i);
            CharData charData = charDataCache.computeIfAbsent(charCode, c -> {
                GlyphInfo info = fontUtils.fontSet.getGlyphInfo(c, false);
                float glyphWidth = info.getAdvance(text.getStyle().isBold());
                BakedGlyphAccessor glyph = fontUtils.getGlyphAccessor(c);
                float glyphUVDiff = glyph.getU1() - glyph.getU0();
                return new CharData(c, info, glyphWidth, glyph, glyphUVDiff);
            });
            float oldX = newX;
            newX += charData.glyphWidth();

            if (newX > xLeft && oldX < xLeft) {
                float diff = xLeft - oldX;
                float scale = (1.0f / charSize * diff);
                float sub = charData.glyphUVDiff() * scale;

                fontUtils.pushUV(charCode);
                charData.glyph().setU0(charData.glyph().getU0() + sub);

                poseStack.pushPose();
                float invScale = 1.0f - scale;
                poseStack.scale(invScale, 1, 1);
                Font.StringRenderOutput sro2 = fontUtils.font.new StringRenderOutput(bufferSource, 0, 0, getColor(), false, poseStack.last().pose(), Font.DisplayMode.NORMAL, packedLight);
                StringDecomposer.iterateFormatted(String.valueOf((char)charCode), text.getStyle(), sro2);
                poseStack.popPose();
                fontUtils.popUV(charCode);
                poseStack.translate(charData.glyphWidth() - (charSize * scale), 0, 0);
                continue;
            } else if (newX > xRight && newX < xRight + CHAR_SIZE * 2) {
                float diff = newX - xRight;
                float charRightSpace = charSize - charData.glyphWidth();
                float totalDiff = diff + charRightSpace;

                float scale = (1.0f / charSize * totalDiff);
                float sub = charData.glyphUVDiff() * scale;

                fontUtils.pushUV(charCode);
                charData.glyph().setU1(charData.glyph().getU1() - sub);
                poseStack.pushPose();
                float invScale = 1.0f - scale;
                poseStack.scale(invScale, 1, 1);
                poseStack.translate(glyphTranslation / invScale, 0, 0);
                Font.StringRenderOutput sro2 = fontUtils.font.new StringRenderOutput(bufferSource, 0, 0, getColor(), false, poseStack.last().pose(), Font.DisplayMode.NORMAL, packedLight);
                StringDecomposer.iterateFormatted(String.valueOf((char)charCode), text.getStyle(), sro2);
                poseStack.popPose();
                fontUtils.popUV(charCode);
                break;
            } else if (oldX >= xLeft && newX <= xRight) {
                StringDecomposer.iterateFormatted(String.valueOf((char)charCode), text.getStyle(), sro);
            } else {
                continue;
            }

            glyphTranslation += charData.glyphWidth();
        }
        poseStack.popPose();
    }
}
