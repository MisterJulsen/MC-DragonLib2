package de.mrjulsen.mcdragonlib.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import de.mrjulsen.mcdragonlib.network.v2.codec.DLStreamCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.DyeColor;

/**
 * Immutable ARGB color with conversion, transformation and blending utilities.
 *
 * <p>Channel inputs are clamped (int: 0–255, float: 0.0–1.0). {@link #UNDEFINED} represents
 * the absence of a color; most methods throw {@link IllegalStateException} when called on it.
 */
public final class DLColor {

    /** Blend modes for {@link DLColor#combine(DLColor, DLColor, CombineMode)}. Only RGB is affected. */
    public enum CombineMode {
        ADD,
        SUBTRACT,
        MULTIPLY,
        /** @deprecated Identical to {@link #SCREEN}. Use {@link #SCREEN} instead. */
        @Deprecated(forRemoval = true)
        NEGATIVE_MULTIPLY,
        SCREEN,
        OVERLAY,
        DIFFERENCE
    }

    /** RGB channel identifiers. */
    public enum ColorChannel {
        R, G, B
    }

    private final int a, r, g, b;
    private final boolean isDefined;

    public static final DLColor WHITE = DLColor.of(255, 255, 255);
    public static final DLColor BLACK = DLColor.of(0, 0, 0);
    public static final DLColor RED = DLColor.of(255, 0, 0);
    public static final DLColor GREEN = DLColor.of(0, 255, 0);
    public static final DLColor BLUE = DLColor.of(0, 0, 255);
    public static final DLColor YELLOW = DLColor.of(255, 255, 0);
    public static final DLColor CYAN = DLColor.of(0, 255, 255);
    public static final DLColor MAGENTA = DLColor.of(255, 0, 255);
    /** Fully transparent black. */
    public static final DLColor TRANSPARENT = DLColor.of(0, 0, 0, 0);
    /** Sentinel for "no color". Only {@link #isUndefined()}, {@code equals}, {@code hashCode} and {@code toString} are safe to call. */
    public static final DLColor UNDEFINED = new DLColor();

    /** Decodes from a packed ARGB int or a hex string, encodes as packed ARGB int. */
    public static final Codec<DLColor> CODEC = Codec.either(Codec.INT, Codec.STRING).comapFlatMap(
        either -> either.map(argb -> DataResult.success(fromInt(argb)), DLColor::parseHexResult),
        color -> Either.left(color.getAsARGB())
    );

    /** Network codec. Supports {@link #UNDEFINED}. */
    public static final DLStreamCodec<DLColor> STREAM_CODEC = DLStreamCodec.of(
        (buf, color) -> {
            buf.writeBoolean(color.isDefined);
            if (color.isDefined) {
                buf.writeInt(color.getAsARGB());
            }
        },
        buf -> buf.readBoolean() ? fromInt(buf.readInt()) : UNDEFINED
    );

    private DLColor(int a, int r, int g, int b) {
        this.a = clamp(a);
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.isDefined = true;
    }

    private DLColor() {
        this.a = 0;
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.isDefined = false;
    }

    /** Creates an opaque color from RGB channels (0–255). */
    public static DLColor of(int r, int g, int b) {
        return new DLColor(255, r, g, b);
    }

    /** Creates a color from ARGB channels (0–255). */
    public static DLColor of(int a, int r, int g, int b) {
        return new DLColor(a, r, g, b);
    }

    /** Creates an opaque color from normalized RGB channels (0.0–1.0). */
    public static DLColor of(float r, float g, float b) {
        return new DLColor(255, toByte(r), toByte(g), toByte(b));
    }

    /** Creates a color from normalized ARGB channels (0.0–1.0). */
    public static DLColor of(float a, float r, float g, float b) {
        return new DLColor(toByte(a), toByte(r), toByte(g), toByte(b));
    }

    /** Creates a color from a packed {@code 0xAARRGGBB} int. */
    public static DLColor fromInt(int argb) {
        return new DLColor((argb >> 24) & 0xFF, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
    }

    /** Creates a color from a packed {@code 0xAABBGGRR} int, as used by {@code NativeImage}. */
    public static DLColor fromABGR(int abgr) {
        return new DLColor((abgr >> 24) & 0xFF, abgr & 0xFF, (abgr >> 8) & 0xFF, (abgr >> 16) & 0xFF);
    }

    /**
     * Parses a hex color string. Accepts {@code RGB}, {@code ARGB}, {@code RRGGBB} and {@code AARRGGBB},
     * optionally prefixed with {@code #} or {@code 0x}.
     *
     * @throws IllegalArgumentException if the string is not a valid hex color
     */
    public static DLColor fromHex(String hexString) {
        return fromInt(parseHex(hexString));
    }

    /** Creates an opaque color from HSV. Hue in degrees (wrapped), saturation and value in 0.0–1.0. */
    public static DLColor fromHsv(float h, float s, float v) {
        h = (h % 360f + 360f) % 360f;
        s = clamp(s);
        v = clamp(v);
        float c = v * s, x = c * (1 - Math.abs((h / 60f) % 2 - 1)), m = v - c;
        float r, g, b;
        if (h < 60) {
            r = c;
            g = x;
            b = 0;
        } else if (h < 120) {
            r = x;
            g = c;
            b = 0;
        } else if (h < 180) {
            r = 0;
            g = c;
            b = x;
        } else if (h < 240) {
            r = 0;
            g = x;
            b = c;
        } else if (h < 300) {
            r = x;
            g = 0;
            b = c;
        } else {
            r = c;
            g = 0;
            b = x;
        }
        return DLColor.of(r + m, g + m, b + m);
    }

    /** Returns the diffuse color of the given dye. */
    public static DLColor fromDyeColor(DyeColor dye) {
        float[] rgb = dye.getTextureDiffuseColors();
        return DLColor.of(rgb[0], rgb[1], rgb[2]);
    }

    /** Returns the text color of the given formatting, or {@link #UNDEFINED} if it has none (e.g. {@code BOLD}). */
    public static DLColor fromChatFormatting(ChatFormatting formatting) {
        Integer rgb = formatting.getColor();
        return rgb == null ? UNDEFINED : fromInt(0xFF000000 | rgb);
    }

    private void checkDefined() {
        if (!isDefined)
            throw new IllegalStateException("Operation cannot be performed on an UNDEFINED color.");
    }

    /** Alpha channel (0–255). */
    public int getAlpha() {
        checkDefined();
        return a;
    }

    /** Red channel (0–255). */
    public int getRed() {
        checkDefined();
        return r;
    }

    /** Green channel (0–255). */
    public int getGreen() {
        checkDefined();
        return g;
    }

    /** Blue channel (0–255). */
    public int getBlue() {
        checkDefined();
        return b;
    }

    /** Alpha channel (0.0–1.0). */
    public float getAlphaF() {
        checkDefined();
        return a / 255.0f;
    }

    /** Red channel (0.0–1.0). */
    public float getRedF() {
        checkDefined();
        return r / 255.0f;
    }

    /** Green channel (0.0–1.0). */
    public float getGreenF() {
        checkDefined();
        return g / 255.0f;
    }

    /** Blue channel (0.0–1.0). */
    public float getBlueF() {
        checkDefined();
        return b / 255.0f;
    }

    /** Hue in degrees (0–360). */
    public float getHue() {
        return getAsHSV()[0];
    }

    /** HSB saturation (0.0–1.0). */
    public float getSaturation() {
        return getAsHSV()[1];
    }

    /** HSB brightness/value (0.0–1.0). */
    public float getBrightness() {
        return getAsHSV()[2];
    }

    /** Hue in whole degrees (0–359). */
    public int getHueInt() {
        return Math.round(getHue()) % 360;
    }

    /** Saturation in percent (0–100). */
    public int getSaturationInt() {
        return Math.round(getSaturation() * 100);
    }

    /** Brightness/value in percent (0–100). */
    public int getValueInt() {
        return Math.round(getBrightness() * 100);
    }

    /** Packed {@code 0xAARRGGBB} int. */
    public int getAsARGB() {
        checkDefined();
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Packed {@code 0xAABBGGRR} int, as used by {@code NativeImage}. */
    public int getAsABGR() {
        checkDefined();
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    /** Uppercase hex string, {@code #AARRGGBB} if {@code includeAlpha}, otherwise {@code #RRGGBB}. */
    public String getAsHEX(boolean includeAlpha) {
        checkDefined();
        return includeAlpha ? String.format("#%02X%02X%02X%02X", a, r, g, b) : String.format("#%02X%02X%02X", r, g, b);
    }

    /** Uppercase hex string. Alpha is only included if the color is not fully opaque. */
    public String getAsHEX() {
        return getAsHEX(hasTransparency());
    }

    @Deprecated
    public float[] getAsHSB() {
        return getAsHSV();
    }

    /** HSB components as {@code {hue (0–360), saturation (0.0–1.0), brightness (0.0–1.0)}}. */
    public float[] getAsHSV() {
        checkDefined();
        float r_ = getRedF(), g_ = getGreenF(), b_ = getBlueF();
        float cmax = Math.max(r_, Math.max(g_, b_)), cmin = Math.min(r_, Math.min(g_, b_));
        float delta = cmax - cmin, h = 0, s;
        if (delta != 0) {
            if (cmax == r_)
                h = 60 * (((g_ - b_) / delta) % 6);
            else if (cmax == g_)
                h = 60 * (((b_ - r_) / delta) + 2);
            else
                h = 60 * (((r_ - g_) / delta) + 4);
        }
        h = (h + 360f) % 360f;
        s = (cmax == 0) ? 0 : (delta / cmax);
        return new float[] { h, s, cmax };
    }

    /** Whether this is the {@link #UNDEFINED} sentinel. */
    public boolean isUndefined() {
        return !this.isDefined;
    }

    /** Whether alpha is below 255. */
    public boolean hasTransparency() {
        return getAlpha() < 255;
    }

    /** Whether alpha is 0. */
    public boolean isTransparent() {
        return getAlpha() == 0;
    }

    /** Blends towards white by {@code amount} (0.0–1.0). */
    public DLColor lighten(float amount) {
        checkDefined();
        return blend(this, WHITE.withAlpha(a), amount);
    }

    /** Blends towards black by {@code amount} (0.0–1.0). */
    public DLColor darken(float amount) {
        checkDefined();
        return blend(this, BLACK.withAlpha(a), amount);
    }

    /** Inverts the RGB channels. Alpha is kept. */
    public DLColor invert() {
        checkDefined();
        return new DLColor(a, 255 - r, 255 - g, 255 - b);
    }

    /** Converts to grayscale using Rec. 601 luma. Alpha is kept. */
    public DLColor grayscale() {
        checkDefined();
        int gray = (int) Math.round(r * 0.299 + g * 0.587 + b * 0.114);
        return new DLColor(a, gray, gray, gray);
    }

    /** Adds {@code amount} to the HSB saturation. Negative values desaturate. Alpha is kept. */
    public DLColor saturate(float amount) {
        float[] hsv = getAsHSV();
        return DLColor.fromHsv(hsv[0], clamp(hsv[1] + amount), hsv[2]).withAlpha(a);
    }

    /** Rotates the hue by the given degrees. Alpha is kept. */
    public DLColor rotateHue(float degrees) {
        float[] hsv = getAsHSV();
        return DLColor.fromHsv(hsv[0] + degrees, hsv[1], hsv[2]).withAlpha(a);
    }

    /** Copy with a different alpha (0–255). */
    public DLColor withAlpha(int newAlpha) {
        checkDefined();
        return new DLColor(newAlpha, r, g, b);
    }

    /** Copy with a different alpha (0.0–1.0). */
    public DLColor withAlpha(float newAlpha) {
        return withAlpha(toByte(newAlpha));
    }

    /** Copy with a different red channel (0–255). */
    public DLColor withRed(int newRed) {
        checkDefined();
        return new DLColor(a, newRed, g, b);
    }

    /** Copy with a different green channel (0–255). */
    public DLColor withGreen(int newGreen) {
        checkDefined();
        return new DLColor(a, r, newGreen, b);
    }

    /** Copy with a different blue channel (0–255). */
    public DLColor withBlue(int newBlue) {
        checkDefined();
        return new DLColor(a, r, g, newBlue);
    }

    /** Swaps two RGB channels. Alpha is kept. */
    public DLColor swapChannels(ColorChannel c1, ColorChannel c2) {
        checkDefined();
        int[] rgb = { r, g, b };
        int tmp = rgb[c1.ordinal()];
        rgb[c1.ordinal()] = rgb[c2.ordinal()];
        rgb[c2.ordinal()] = tmp;
        return new DLColor(a, rgb[0], rgb[1], rgb[2]);
    }

    /** Perceived brightness (Rec. 601 luma, 0.0–1.0). */
    public float getLuminance() {
        checkDefined();
        return (0.299f * getRedF()) + (0.587f * getGreenF()) + (0.114f * getBlueF());
    }

    /** Whether {@link #getLuminance()} is above the given threshold. */
    public boolean isLight(float threshold) {
        return getLuminance() > threshold;
    }

    /** Linear interpolation of all ARGB channels. {@code factor} 0.0 returns {@code color1}, 1.0 returns {@code color2}. */
    public static DLColor blend(DLColor color1, DLColor color2, float factor) {
        color1.checkDefined();
        color2.checkDefined();
        factor = clamp(factor);
        float t = 1.0f - factor;
        return new DLColor(
            Math.round(color1.a * t + color2.a * factor),
            Math.round(color1.r * t + color2.r * factor),
            Math.round(color1.g * t + color2.g * factor),
            Math.round(color1.b * t + color2.b * factor)
        );
    }

    /** Combines the RGB channels of two colors using the given mode. Alpha is averaged. */
    @SuppressWarnings("removal")
    public static DLColor combine(DLColor c1, DLColor c2, CombineMode mode) {
        c1.checkDefined();
        c2.checkDefined();
        float r1 = c1.getRedF(), g1 = c1.getGreenF(), b1 = c1.getBlueF(), r2 = c2.getRedF(), g2 = c2.getGreenF(), b2 = c2.getBlueF();
        float resR, resG, resB;
        switch (mode) {
            case ADD:
                resR = r1 + r2;
                resG = g1 + g2;
                resB = b1 + b2;
                break;
            case SUBTRACT:
                resR = r1 - r2;
                resG = g1 - g2;
                resB = b1 - b2;
                break;
            case MULTIPLY:
                resR = r1 * r2;
                resG = g1 * g2;
                resB = b1 * b2;
                break;
            case NEGATIVE_MULTIPLY:
            case SCREEN:
                resR = 1 - (1 - r1) * (1 - r2);
                resG = 1 - (1 - g1) * (1 - g2);
                resB = 1 - (1 - b1) * (1 - b2);
                break;
            case OVERLAY:
                resR = r1 < 0.5 ? 2 * r1 * r2 : 1 - 2 * (1 - r1) * (1 - r2);
                resG = g1 < 0.5 ? 2 * g1 * g2 : 1 - 2 * (1 - g1) * (1 - g2);
                resB = b1 < 0.5 ? 2 * b1 * b2 : 1 - 2 * (1 - b1) * (1 - b2);
                break;
            case DIFFERENCE:
                resR = Math.abs(r1 - r2);
                resG = Math.abs(g1 - g2);
                resB = Math.abs(b1 - b2);
                break;
            default:
                throw new UnsupportedOperationException("CombineMode " + mode + " not implemented.");
        }
        return DLColor.of((c1.a + c2.a) / 510f, resR, resG, resB);
    }

    /** Composites {@code foreground} over {@code background} (Porter-Duff "over"). */
    public static DLColor alphaBlend(DLColor foreground, DLColor background) {
        foreground.checkDefined();
        background.checkDefined();
        float fgA = foreground.getAlphaF(), bgA = background.getAlphaF(), outA = fgA + bgA * (1 - fgA);
        if (outA == 0)
            return TRANSPARENT;
        float r = (foreground.getRedF() * fgA + background.getRedF() * bgA * (1 - fgA)) / outA;
        float g = (foreground.getGreenF() * fgA + background.getGreenF() * bgA * (1 - fgA)) / outA;
        float b = (foreground.getBlueF() * fgA + background.getBlueF() * bgA * (1 - fgA)) / outA;
        return DLColor.of(outA, r, g, b);
    }

    /** Mixes two colors into a tint that leans towards the dominant channels. Alpha is averaged. */
    public static DLColor mixTint(DLColor colorA, DLColor colorB) {
        colorA.checkDefined();
        colorB.checkDefined();
        float[] A = { colorA.getRedF(), colorA.getGreenF(), colorA.getBlueF() };
        float[] B = { colorB.getRedF(), colorB.getGreenF(), colorB.getBlueF() };
        float w = Math.max(A[0], B[0]) * Math.max(A[1], B[1]) * Math.max(A[2], B[2]);
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = Math.max(A[i], B[i]) * (1f - w) + Math.min(A[i], B[i]) * w;
        }
        return DLColor.of((colorA.getAlphaF() + colorB.getAlphaF()) / 2f, result[0], result[1], result[2]);
    }

    /** Returns {@code darkColor} if {@code base} is light, otherwise {@code lightColor}. Useful for readable text on a background. */
    public static DLColor pickBasedOnBrightness(DLColor base, DLColor lightColor, DLColor darkColor, float threshold) {
        return base.isLight(threshold) ? darkColor : lightColor;
    }

    /** Euclidean distance in RGB space (not perceptual). */
    public static double distance(DLColor c1, DLColor c2) {
        c1.checkDefined();
        c2.checkDefined();
        int dr = c1.r - c2.r, dg = c1.g - c2.g, db = c1.b - c2.b;
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static int toByte(float v) {
        return (int) (clamp(v) * 255f + 0.5f);
    }

    private static DataResult<DLColor> parseHexResult(String hex) {
        try {
            return DataResult.success(fromHex(hex));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }

    private static int parseHex(String hex) {
        if (hex == null)
            throw new IllegalArgumentException("HEX color string must not be null.");
        String clean = hex.trim();
        if (clean.startsWith("#"))
            clean = clean.substring(1);
        else if (clean.startsWith("0x") || clean.startsWith("0X"))
            clean = clean.substring(2);

        if (!clean.matches("[0-9a-fA-F]+"))
            throw new IllegalArgumentException("Invalid HEX color string: " + hex);

        if (clean.length() == 3 || clean.length() == 4) {
            StringBuilder sb = new StringBuilder();
            for (char c : clean.toCharArray()) {
                sb.append(c).append(c);
            }
            clean = sb.toString();
        }
        if (clean.length() == 6)
            clean = "FF" + clean;
        if (clean.length() != 8)
            throw new IllegalArgumentException("Invalid HEX color string: " + hex);

        return (int) Long.parseLong(clean, 16);
    }

    /** Two colors are equal if all ARGB channels match. {@link #UNDEFINED} is only equal to itself. */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DLColor c = (DLColor) o;
        if (!this.isDefined || !c.isDefined)
            return this.isDefined == c.isDefined;
        return a == c.a && r == c.r && g == c.g && b == c.b;
    }

    @Override
    public int hashCode() {
        return isDefined ? getAsARGB() : -1;
    }

    @Override
    public String toString() {
        return isDefined ? String.format("DLColor[A=%d, R=%d, G=%d, B=%d]", a, r, g, b) : "DLColor[UNDEFINED]";
    }
}
