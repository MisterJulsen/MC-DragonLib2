package de.mrjulsen.mcdragonlib.util;

/**
 * A versatile, immutable class for representing and manipulating ARGB colors.
 * Instances are created via static factory methods (e.g., DLColor.of,
 * DLColor.fromHex).
 * It allows for conversions and a variety of manipulations like blending,
 * lightening,
 * changing saturation/hue, and much more.
 */
public final class Color {

    public enum CombineMode {
        ADD, SUBTRACT, MULTIPLY, NEGATIVE_MULTIPLY, SCREEN, OVERLAY, DIFFERENCE
    }

    public enum ColorChannel {
        R, G, B
    }

    private final int a, r, g, b;
    /** Flag to distinguish between a defined color and the UNDEFINED singleton. */
    private final boolean isDefined;

    // --- Predefined Color Constants ---
    public static final Color WHITE = Color.of(255, 255, 255);
    public static final Color BLACK = Color.of(0, 0, 0);
    public static final Color RED = Color.of(255, 0, 0);
    public static final Color GREEN = Color.of(0, 255, 0);
    public static final Color BLUE = Color.of(0, 0, 255);
    public static final Color YELLOW = Color.of(255, 255, 0);
    public static final Color CYAN = Color.of(0, 255, 255);
    public static final Color MAGENTA = Color.of(255, 0, 255);
    /** A fully transparent black color (ARGB: 0x00000000). */
    public static final Color TRANSPARENT = Color.of(0, 0, 0, 0);
    /** A special constant representing an undefined or invalid color. */
    public static final Color UNDEFINED = new Color();

    /**
     * Main constructor for defined colors.
     */
    private Color(int a, int r, int g, int b) {
        this.a = clamp(a);
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.isDefined = true;
    }

    /**
     * Special private constructor only for the UNDEFINED singleton.
     */
    private Color() {
        this.a = 0;
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.isDefined = false;
    }

    /** Creates an opaque color from RGB integer values (0-255). */
    public static Color of(int r, int g, int b) {
        return new Color(255, r, g, b);
    }

    /** Creates a color from ARGB integer values (0-255). */
    public static Color of(int a, int r, int g, int b) {
        return new Color(a, r, g, b);
    }

    /** Creates an opaque color from RGB float values (0.0f-1.0f). */
    public static Color of(float r, float g, float b) {
        return new Color(255, (int) (clamp(r) * 255f + 0.5f), (int) (clamp(g) * 255f + 0.5f), (int) (clamp(b) * 255f + 0.5f));
    }

    /** Creates a color from ARGB float values (0.0f-1.0f). */
    public static Color of(float a, float r, float g, float b) {
        return new Color((int) (clamp(a) * 255f + 0.5f), (int) (clamp(r) * 255f + 0.5f), (int) (clamp(g) * 255f + 0.5f), (int) (clamp(b) * 255f + 0.5f));
    }

    /** Creates a color from a packed 32-bit ARGB integer (0xAARRGGBB). */
    public static Color fromInt(int argb) {
        return new Color((argb >> 24) & 0xFF, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
    }

    /** Creates a color from a HEX string (#RGB, #RRGGBB, #ARGB, #AARRGGBB). */
    public static Color fromHex(String hexString) {
        int[] c = parseHex(hexString);
        return new Color(c[0], c[1], c[2], c[3]);
    }

    /** Creates a color from HSV values (Hue 0-360, Saturation 0-1, Value 0-1). */
    public static Color fromHsv(float h, float s, float v) {
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
        return Color.of(r + m, g + m, b + m);
    }

    // --- Getters & Conversions ---
    private void checkDefined() {
        if (!isDefined)
            throw new IllegalStateException("Operation cannot be performed on an UNDEFINED color.");
    }

    public int getAlpha() {
        checkDefined();
        return a;
    }

    public int getRed() {
        checkDefined();
        return r;
    }

    public int getGreen() {
        checkDefined();
        return g;
    }

    public int getBlue() {
        checkDefined();
        return b;
    }

    public float getAlphaF() {
        checkDefined();
        return a / 255.0f;
    }

    public float getRedF() {
        checkDefined();
        return r / 255.0f;
    }

    public float getGreenF() {
        checkDefined();
        return g / 255.0f;
    }

    public float getBlueF() {
        checkDefined();
        return b / 255.0f;
    }

    public float getHue() {
        checkDefined();
        return getAsHSB()[0];
    }

    public float getSaturation() {
        checkDefined();
        return getAsHSB()[1];
    }

    public float getBrightness() {
        checkDefined();
        return getAsHSB()[2];
    }

    /** Returns the hue component as an integer (0-360). */
    public int getHueInt() {
        checkDefined();
        return (int) getHue();
    }

    /** Returns the saturation component as an integer (0-100). */
    public int getSaturationInt() {
        checkDefined();
        return Math.round(getSaturation() * 100);
    }

    /** Returns the value/brightness component as an integer (0-100). */
    public int getValueInt() {
        checkDefined();
        return Math.round(getBrightness() * 100);
    }

    public int getAsARGB() {
        checkDefined();
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int getAsInt() {
        return getAsARGB();
    }

    public String getAsHEX(boolean includeAlpha) {
        checkDefined();
        return includeAlpha ? String.format("#%02X%02X%02X%02X", a, r, g, b) : String.format("#%02X%02X%02X", r, g, b);
    }

    public float[] getAsHSB() {
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

    /** Checks if this color is the special UNDEFINED constant. */
    public boolean isUndefined() {
        return !this.isDefined;
    }

    // --- Instance Methods (Modifications) ---

    public Color lighten(float amount) {
        checkDefined();
        return blend(this, WHITE, amount);
    }

    public Color darken(float amount) {
        checkDefined();
        return blend(this, BLACK, amount);
    }

    public Color invert() {
        checkDefined();
        return new Color(a, 255 - r, 255 - g, 255 - b);
    }

    public Color grayscale() {
        checkDefined();
        int gray = (int) Math.round(r * 0.299 + g * 0.587 + b * 0.114);
        return new Color(a, gray, gray, gray);
    }

    public Color saturate(float amount) {
        checkDefined();
        float[] hsv = getAsHSB();
        hsv[1] = clamp(hsv[1] + amount);
        return Color.fromHsv(hsv[0], hsv[1], hsv[2]).withAlpha(this.a);
    }

    public Color rotateHue(float degrees) {
        checkDefined();
        float[] hsv = getAsHSB();
        hsv[0] = (hsv[0] + degrees) % 360f;
        return Color.fromHsv(hsv[0], hsv[1], hsv[2]).withAlpha(this.a);
    }

    public Color withAlpha(int newAlpha) {
        checkDefined();
        return new Color(newAlpha, r, g, b);
    }

    public Color swapChannels(ColorChannel c1, ColorChannel c2) {
        checkDefined();
        int red = r, green = g, blue = b;
        int val1 = getChannelValue(c1), val2 = getChannelValue(c2);
        red = setChannelValue(ColorChannel.R, c1, val2, setChannelValue(ColorChannel.R, c2, val1, red));
        green = setChannelValue(ColorChannel.G, c1, val2, setChannelValue(ColorChannel.G, c2, val1, green));
        blue = setChannelValue(ColorChannel.B, c1, val2, setChannelValue(ColorChannel.B, c2, val1, blue));
        return new Color(a, red, green, blue);
    }

    public float getLuminance() {
        checkDefined();
        return (0.299f * getRedF()) + (0.587f * getGreenF()) + (0.114f * getBlueF());
    }

    public boolean isLight(float threshold) {
        checkDefined();
        return getLuminance() > threshold;
    }

    // --- Static Utility Methods ---

    /** Blends two colors based on a factor. 0.0 = color1, 1.0 = color2. */
    public static Color blend(Color color1, Color color2, float factor) {
        color1.checkDefined();
        color2.checkDefined();
        factor = clamp(factor);
        float t_ = 1.0f - factor;
        int a = (int) (color1.a * t_ + color2.a * factor), r = (int) (color1.r * t_ + color2.r * factor);
        int g = (int) (color1.g * t_ + color2.g * factor), b = (int) (color1.b * t_ + color2.b * factor);
        return new Color(a, r, g, b);
    }

    /** Combines two colors using a specific mode. */
    public static Color combine(Color c1, Color c2, CombineMode mode) {
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
            case MULTIPLY: // TODO remove clamp
                resR = MathUtils.clamp(r1 * r2, 0, 255);
                resG = MathUtils.clamp(g1 * g2, 0, 255);
                resB = MathUtils.clamp(b1 * b2, 0, 255);
                break;
            case NEGATIVE_MULTIPLY:
                resR = 1 - (1 - r1) * (1 - r2);
                resG = 1 - (1 - g1) * (1 - g2);
                resB = 1 - (1 - b1) * (1 - b2);
                break;
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
        return Color.of((c1.a + c2.a) / 510f, resR, resG, resB);
    }

    /** Overlays a foreground color onto a background color using alpha blending. */
    public static Color alphaBlend(Color foreground, Color background) {
        foreground.checkDefined();
        background.checkDefined();
        float fgA = foreground.getAlphaF(), bgA = background.getAlphaF(), outA = fgA + bgA * (1 - fgA);
        if (outA == 0)
            return TRANSPARENT;
        float r = (foreground.getRedF() * fgA + background.getRedF() * bgA * (1 - fgA)) / outA;
        float g = (foreground.getGreenF() * fgA + background.getGreenF() * bgA * (1 - fgA)) / outA;
        float b = (foreground.getBlueF() * fgA + background.getBlueF() * bgA * (1 - fgA)) / outA;
        return Color.of(outA, r, g, b);
    }

    public static Color mixTint(Color colorA, Color colorB) {
        float alphaA = colorA.getAlphaF();
        float alphaB = colorB.getAlphaF();
        float[] A = new float[] { colorA.getRedF(), colorA.getGreenF(), colorA.getBlueF() };
        float[] B = new float[] { colorB.getRedF(), colorB.getGreenF(), colorB.getBlueF() };
        float w = 1f;
        for (int i = 0; i < 3; i++) {
            float maxVal = Math.max(A[i], B[i]);
            float minVal = Math.min(maxVal, 1f);
            w *= minVal;
        }

        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = Math.max(A[i], B[i]) * (1f - w) + Math.min(A[i], B[i]) * w;
        }
        return Color.of((alphaA + alphaB) / 2f, result[0], result[1], result[2]);
    }

    /** Selects one of two colors based on the brightness of a base color. */
    public static Color pickBasedOnBrightness(Color base, Color lightColor, Color darkColor, float threshold) {
        base.checkDefined();
        return base.isLight(threshold) ? darkColor : lightColor;
    }

    /**
     * Calculates the euclidean distance between two colors in the RGB space
     * (0-441.67).
     */
    public static double distance(Color c1, Color c2) {
        c1.checkDefined();
        c2.checkDefined();
        int dr = c1.r - c2.r, dg = c1.g - c2.g, db = c1.b - c2.b;
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    // --- Private Helper Methods ---
    private int getChannelValue(ColorChannel channel) {
        checkDefined();
        switch (channel) {
            case R:
                return r;
            case G:
                return g;
            case B:
                return b;
            default:
                throw new IllegalArgumentException();
        }
    }

    private int setChannelValue(ColorChannel target, ColorChannel source, int value, int current) {
        return target == source ? value : current;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static int[] parseHex(String hex) {
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        int a = 255, r, g, b;
        try {
            if (clean.length() == 3) {
                r = Integer.parseInt(clean.substring(0, 1) + clean.substring(0, 1), 16);
                g = Integer.parseInt(clean.substring(1, 2) + clean.substring(1, 2), 16);
                b = Integer.parseInt(clean.substring(2, 3) + clean.substring(2, 3), 16);
            } else if (clean.length() == 4) {
                a = Integer.parseInt(clean.substring(0, 1) + clean.substring(0, 1), 16);
                r = Integer.parseInt(clean.substring(1, 2) + clean.substring(1, 2), 16);
                g = Integer.parseInt(clean.substring(2, 3) + clean.substring(2, 3), 16);
                b = Integer.parseInt(clean.substring(3, 4) + clean.substring(3, 4), 16);
            } else if (clean.length() == 6) {
                r = Integer.parseInt(clean.substring(0, 2), 16);
                g = Integer.parseInt(clean.substring(2, 4), 16);
                b = Integer.parseInt(clean.substring(4, 6), 16);
            } else if (clean.length() == 8) {
                a = Integer.parseInt(clean.substring(0, 2), 16);
                r = Integer.parseInt(clean.substring(2, 4), 16);
                g = Integer.parseInt(clean.substring(4, 6), 16);
                b = Integer.parseInt(clean.substring(6, 8), 16);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid HEX color string: " + hex, e);
        }
        return new int[] { a, r, g, b };
    }

    // --- Overridden Standard Methods ---
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Color c = (Color) o;
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
