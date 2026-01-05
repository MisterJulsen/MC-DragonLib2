package de.mrjulsen.mcdragonlib.util;

import de.mrjulsen.mcdragonlib.util.math.MathUtils;

/**
 * Immutable ARGB color utility and value object.
 *
 * <p>This final class represents a color using four 8-bit channels (Alpha, Red, Green, Blue).
 * Instances are immutable and may be created via the provided factory methods. A special
 * sentinel instance {@link #UNDEFINED} exists to represent the absence of a color; calling
 * most instance methods on that sentinel will throw {@link IllegalStateException}.
 *
 * <p>The class exposes:
 * <ul>
 *   <li>Factory methods for integer and normalized float components, packed ARGB ints,
 *       CSS-like hex strings and HSV.</li>
 *   <li>Accessors for raw 0–255 channels and normalized 0.0–1.0 floats, plus HSB/HSV helpers.</li>
 *   <li>Immutable color transforms (lighten, darken, invert, grayscale, saturate, rotateHue, etc.).</li>
 *   <li>Pixel/compositing helpers (blend, alphaBlend, combine, mixTint).</li>
 *   <li>Utility predicates and equality/hash contract that treat UNDEFINED specially.</li>
 * </ul>
 *
 * <p>Thread-safety: immutable and therefore inherently thread-safe.
 *
 * <p>Usage notes:
 * <ul>
 *   <li>All float inputs are clamped to the range [0.0, 1.0] where documented.</li>
 *   <li>Integer channel inputs are clamped to [0, 255].</li>
 *   <li>Methods returning new colors do not mutate the receiver.</li>
 * </ul>
 */
public final class DLColor {
    /**
     * Modes describing how two colors are combined component-wise.
     *
     * <p>Combine algorithms follow commonly used blending semantics:
     * <ul>
     *   <li>ADD/SUBTRACT operate on normalized channels by addition/subtraction.</li>
     *   <li>MULTIPLY multiplies normalized channels (darkening blend).</li>
     *   <li>NEGATIVE_MULTIPLY and SCREEN implement two common screen-like formulas.</li>
     *   <li>OVERLAY applies a contrast-preserving overlay.</li>
     *   <li>DIFFERENCE produces the absolute-channel difference.</li>
     * </ul>
     *
     * <p>All combine modes operate on RGB channels only; alpha handling is separate and
     * implementations in this class generally combine alpha as a simple mean or preserve
     * the original values as documented on the calling method.
     */
    public enum CombineMode {
        ADD, SUBTRACT, MULTIPLY, NEGATIVE_MULTIPLY, SCREEN, OVERLAY, DIFFERENCE
    }

    /**
     * Identifiers for RGB channels used by channel-manipulation helpers.
     *
     * <p>Values refer to Red, Green and Blue channels respectively and are used by
     * methods that need to select or swap channels dynamically.
     */
    public enum ColorChannel {
        R, G, B
    }

    private final int a, r, g, b;
    /** Flag to distinguish between a defined color and the UNDEFINED singleton. */
    private final boolean isDefined;

    // --- Predefined Color Constants ---
    /**
     * A fully opaque white color (ARGB 0xFFFFFFFF).
     */
    public static final DLColor WHITE = DLColor.of(255, 255, 255);

    /**
     * A fully opaque black color (ARGB 0xFF000000).
     */
    public static final DLColor BLACK = DLColor.of(0, 0, 0);

    /**
     * A fully opaque red color (ARGB 0xFFFF0000).
     */
    public static final DLColor RED = DLColor.of(255, 0, 0);

    /**
     * A fully opaque green color (ARGB 0xFF00FF00).
     */
    public static final DLColor GREEN = DLColor.of(0, 255, 0);

    /**
     * A fully opaque blue color (ARGB 0xFF0000FF).
     */
    public static final DLColor BLUE = DLColor.of(0, 0, 255);

    /**
     * A fully opaque yellow color (ARGB 0xFFFFFF00).
     */
    public static final DLColor YELLOW = DLColor.of(255, 255, 0);

    /**
     * A fully opaque cyan color (ARGB 0xFF00FFFF).
     */
    public static final DLColor CYAN = DLColor.of(0, 255, 255);

    /**
     * A fully opaque magenta color (ARGB 0xFFFF00FF).
     */
    public static final DLColor MAGENTA = DLColor.of(255, 0, 255);

    /**
     * A fully transparent black (ARGB 0x00000000). Alpha = 0, RGB = 0.
     *
     * <p>This is useful as a neutral transparent color for compositing operations.
     */
    public static final DLColor TRANSPARENT = DLColor.of(0, 0, 0, 0);

    /**
     * A sentinel value that denotes an undefined or absent color.
     *
     * <p>Methods that operate on colors will throw {@link IllegalStateException}
     * when invoked on this sentinel, except for {@link #isUndefined()} which
     * explicitly checks for it. Use this constant to represent "no color".
     */
    public static final DLColor UNDEFINED = new DLColor();

    /**
     * Main constructor for defined colors.
     */
    private DLColor(int a, int r, int g, int b) {
        this.a = clamp(a);
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.isDefined = true;
    }

    /**
     * Special private constructor only for the UNDEFINED singleton.
     */
    private DLColor() {
        this.a = 0;
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.isDefined = false;
    }

    /**
     * Create an opaque color from integer RGB components (0–255).
     *
     * @param r the red channel in [0,255]
     * @param g the green channel in [0,255]
     * @param b the blue channel in [0,255]
     * @return a new DLColor instance with alpha = 255 and the specified RGB channels
     */
    public static DLColor of(int r, int g, int b) {
        return new DLColor(255, r, g, b);
    }

    /**
     * Create a color from explicit ARGB integer components (0–255).
     *
     * @param a the alpha channel in [0,255]
     * @param r the red channel in [0,255]
     * @param g the green channel in [0,255]
     * @param b the blue channel in [0,255]
     * @return a new DLColor instance representing the supplied ARGB channels
     */
    public static DLColor of(int a, int r, int g, int b) {
        return new DLColor(a, r, g, b);
    }

    /**
     * Create an opaque color from normalized float RGB components.
     *
     * <p>Float values are expected in the range [0.0, 1.0]. Values outside that range
     * will be clamped. The returned color has alpha = 255 (fully opaque).
     *
     * @param r normalized red channel in [0.0,1.0]
     * @param g normalized green channel in [0.0,1.0]
     * @param b normalized blue channel in [0.0,1.0]
     * @return a new DLColor instance with alpha = 255
     */
    public static DLColor of(float r, float g, float b) {
        return new DLColor(255, (int) (clamp(r) * 255f + 0.5f), (int) (clamp(g) * 255f + 0.5f), (int) (clamp(b) * 255f + 0.5f));
    }

    /**
     * Create a color from normalized ARGB float components.
     *
     * <p>Each component should be in [0.0,1.0]; values will be clamped. Returned
     * channels are converted to 8-bit integers with rounding.
     *
     * @param a normalized alpha in [0.0,1.0]
     * @param r normalized red in [0.0,1.0]
     * @param g normalized green in [0.0,1.0]
     * @param b normalized blue in [0.0,1.0]
     * @return a new DLColor instance representing the specified ARGB color
     */
    public static DLColor of(float a, float r, float g, float b) {
        return new DLColor((int) (clamp(a) * 255f + 0.5f), (int) (clamp(r) * 255f + 0.5f), (int) (clamp(g) * 255f + 0.5f), (int) (clamp(b) * 255f + 0.5f));
    }

    /**
     * Create a DLColor from a packed ARGB integer (0xAARRGGBB).
     *
     * @param argb packed 32-bit ARGB value
     * @return a new DLColor with channels extracted from the packed value
     */
    public static DLColor fromInt(int argb) {
        return new DLColor((argb >> 24) & 0xFF, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
    }

    /**
     * Parse a hex color string and return the corresponding DLColor.
     *
     * <p>Supported formats (with or without leading '#'):
     * <ul>
     *   <li>#RGB (3 hex digits) – shorthand for RRGGBB with alpha = 255</li>
     *   <li>#RRGGBB (6 hex digits) – RGB with alpha = 255</li>
     *   <li>#ARGB (4 hex digits) – shorthand including alpha</li>
     *   <li>#AARRGGBB (8 hex digits) – explicit alpha + RGB</li>
     * </ul>
     *
     * @param hexString the hex color string to parse
     * @return a new DLColor representing the parsed color
     * @throws IllegalArgumentException if the format is not recognized or contains invalid hex digits
     */
    public static DLColor fromHex(String hexString) {
        int[] c = parseHex(hexString);
        return new DLColor(c[0], c[1], c[2], c[3]);
    }

    /**
     * Create a color from HSV values.
     *
     * <p>Hue is specified in degrees and will be normalized to the range [0,360).
     * Saturation and value (brightness) are expected in [0.0,1.0] and are clamped.
     * The produced color is opaque (alpha = 255).
     *
     * @param h hue in degrees (may be outside 0..360; will be wrapped)
     * @param s saturation in [0.0,1.0]
     * @param v value/brightness in [0.0,1.0]
     * @return an opaque DLColor corresponding to the HSV input
     */
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

    // --- Getters & Conversions ---
    private void checkDefined() {
        if (!isDefined)
            throw new IllegalStateException("Operation cannot be performed on an UNDEFINED color.");
    }

    /**
     * Returns the alpha channel as an integer in range 0–255.
     *
     * @return alpha channel (0 = fully transparent, 255 = fully opaque)
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public int getAlpha() {
        checkDefined();
        return a;
    }

    /**
     * Returns the red channel as an integer in range 0–255.
     *
     * @return red channel
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public int getRed() {
        checkDefined();
        return r;
    }

    /**
     * Returns the green channel as an integer in range 0–255.
     *
     * @return green channel
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public int getGreen() {
        checkDefined();
        return g;
    }

    /**
     * Returns the blue channel as an integer in range 0–255.
     *
     * @return blue channel
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public int getBlue() {
        checkDefined();
        return b;
    }

    /**
     * Returns the normalized alpha channel in the range [0.0f, 1.0f].
     *
     * @return alpha as a float fraction of 255
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public float getAlphaF() {
        checkDefined();
        return a / 255.0f;
    }

    /**
     * Returns the normalized red channel in the range [0.0f, 1.0f].
     *
     * @return red normalized to [0,1]
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public float getRedF() {
        checkDefined();
        return r / 255.0f;
    }

    /**
     * Returns the normalized green channel in the range [0.0f, 1.0f].
     *
     * @return green normalized to [0,1]
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public float getGreenF() {
        checkDefined();
        return g / 255.0f;
    }

    /**
     * Returns the normalized blue channel in the range [0.0f, 1.0f].
     *
     * @return blue normalized to [0,1]
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public float getBlueF() {
        checkDefined();
        return b / 255.0f;
    }

    /**
     * Returns the hue component (in degrees 0..360) of this color in HSB/HSV representation.
     *
     * @return hue in degrees
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public float getHue() {
        checkDefined();
        return getAsHSB()[0];
    }

    /**
     * Returns the saturation component of this color in HSB/HSV representation.
     *
     * @return saturation in [0.0,1.0]
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public float getSaturation() {
        checkDefined();
        return getAsHSB()[1];
    }

    /**
     * Returns the brightness/value component of this color in HSB/HSV representation.
     *
     * @return brightness/value in [0.0,1.0]
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
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

    /**
     * Returns the packed ARGB integer equivalent (0xAARRGGBB).
     *
     * @return packed 32-bit ARGB value
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public int getAsARGB() {
        checkDefined();
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Returns a HEX string representation of this color.
     *
     * @param includeAlpha if true returns "#AARRGGBB", otherwise returns "#RRGGBB"
     * @return uppercase hex string with leading '#'
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public String getAsHEX(boolean includeAlpha) {
        checkDefined();
        return includeAlpha ? String.format("#%02X%02X%02X%02X", a, r, g, b) : String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * Returns the HSB components as a float array {h, s, b}.
     *
     * <p>h in degrees [0,360), s and b in [0.0,1.0].
     *
     * @return three-element float array {hue, saturation, brightness}
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
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

    /**
     * Returns true if this instance is the sentinel {@link #UNDEFINED}.
     *
     * @return true for the UNDEFINED instance, false otherwise
     */
    public boolean isUndefined() {
        return !this.isDefined;
    }

    /**
     * Returns true if the alpha channel is less than fully opaque.
     *
     * @return true when alpha normalized {@code < 1.0}
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public boolean hasTransparency() {
        return getAlphaF() < 1;
    }

    /**
     * Returns true if the color is fully transparent (alpha == 0).
     *
     * @return true when alpha normalized {@code <= 0.0}
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public boolean isTransparent() {
        return getAlphaF() <= 0;
    }

    // Transformations (each returns a new DLColor)

    /**
     * Lighten this color by blending it towards white.
     *
     * <p>A factor of 0.0 returns the original color; 1.0 returns pure white. Intermediate
     * values produce a linear interpolation in RGBA space.
     *
     * @param amount blend factor in [0.0,1.0] (clamped)
     * @return a new DLColor representing the lightened color
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public DLColor lighten(float amount) {
        checkDefined();
        return blend(this, WHITE, amount);
    }

    /**
     * Darken this color by blending it towards black.
     *
     * <p>Semantics analogous to {@link #lighten(float)} but towards black.
     *
     * @param amount blend factor in [0.0,1.0] (clamped)
     * @return a new DLColor representing the darkened color
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public DLColor darken(float amount) {
        checkDefined();
        return blend(this, BLACK, amount);
    }

    /**
     * Returns the color with RGB channels inverted; alpha preserved.
     *
     * @return a new DLColor with each RGB channel replaced by 255 - channel
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public DLColor invert() {
        checkDefined();
        return new DLColor(a, 255 - r, 255 - g, 255 - b);
    }

    /**
     * Convert this color to grayscale using luminance coefficients (Rec. 601 luma).
     *
     * <p>Alpha is preserved.
     *
     * @return a new DLColor in grayscale
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public DLColor grayscale() {
        checkDefined();
        int gray = (int) Math.round(r * 0.299 + g * 0.587 + b * 0.114);
        return new DLColor(a, gray, gray, gray);
    }

    /**
     * Increase or decrease saturation by an additive amount.
     *
     * <p>Amount is added to the HSB saturation and clamped to [0.0,1.0].
     * Alpha is preserved on the returned color.
     *
     * @param amount additive saturation change, positive to saturate more, negative to desaturate
     * @return a new DLColor with adjusted saturation
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public DLColor saturate(float amount) {
        checkDefined();
        float[] hsv = getAsHSB();
        hsv[1] = clamp(hsv[1] + amount);
        return DLColor.fromHsv(hsv[0], hsv[1], hsv[2]).withAlpha(this.a);
    }

    /**
     * Rotate the hue by the specified degrees.
     *
     * <p>Hue wraps modulo 360. Alpha is preserved.
     *
     * @param degrees degrees to add to the hue (can be negative)
     * @return a new DLColor with rotated hue
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public DLColor rotateHue(float degrees) {
        checkDefined();
        float[] hsv = getAsHSB();
        hsv[0] = (hsv[0] + degrees) % 360f;
        return DLColor.fromHsv(hsv[0], hsv[1], hsv[2]).withAlpha(this.a);
    }

    /**
     * Return a copy of this color with a different alpha channel.
     *
     * @param newAlpha alpha in integer range [0,255]
     * @return a new DLColor with the same RGB channels and the supplied alpha
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public DLColor withAlpha(int newAlpha) {
        checkDefined();
        return new DLColor(newAlpha, r, g, b);
    }

    /**
     * Swap two RGB channels and return the resulting color.
     *
     * <p>Only RGB channels are affected; alpha remains unchanged.
     *
     * @param c1 first channel to swap
     * @param c2 second channel to swap
     * @return a new DLColor with c1 and c2 exchanged
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public DLColor swapChannels(ColorChannel c1, ColorChannel c2) {
        checkDefined();
        int red = r, green = g, blue = b;
        int val1 = getChannelValue(c1), val2 = getChannelValue(c2);
        red = setChannelValue(ColorChannel.R, c1, val2, setChannelValue(ColorChannel.R, c2, val1, red));
        green = setChannelValue(ColorChannel.G, c1, val2, setChannelValue(ColorChannel.G, c2, val1, green));
        blue = setChannelValue(ColorChannel.B, c1, val2, setChannelValue(ColorChannel.B, c2, val1, blue));
        return new DLColor(a, red, green, blue);
    }

    /**
     * Compute relative luminance using standard coefficients on linearized RGB (here approximated by gamma-encoded channels).
     *
     * @return luminance in the range [0.0f,1.0f]
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public float getLuminance() {
        checkDefined();
        return (0.299f * getRedF()) + (0.587f * getGreenF()) + (0.114f * getBlueF());
    }

    /**
     * Return whether this color is considered "light" compared to a threshold.
     *
     * @param threshold luminance threshold in [0.0,1.0] (caller-defined)
     * @return true if luminance &gt; threshold
     * @throws IllegalStateException if called on {@link #UNDEFINED}
     */
    public boolean isLight(float threshold) {
        checkDefined();
        return getLuminance() > threshold;
    }

    // --- Static Utility Methods ---

    /**
     * Linearly blend two colors by the given factor.
     *
     * <p>Factor 0.0 returns color1, 1.0 returns color2. Alpha channels are blended linearly
     * in the same manner as RGB channels. Inputs must be defined colors.
     *
     * @param color1 first color (factor==0.0 result)
     * @param color2 second color (factor==1.0 result)
     * @param factor interpolation factor in [0.0,1.0]
     * @return a new DLColor representing the interpolated color
     * @throws IllegalStateException if either color is {@link #UNDEFINED}
     */
    public static DLColor blend(DLColor color1, DLColor color2, float factor) {
        color1.checkDefined();
        color2.checkDefined();
        factor = clamp(factor);
        float t_ = 1.0f - factor;
        int a = (int) (color1.a * t_ + color2.a * factor), r = (int) (color1.r * t_ + color2.r * factor);
        int g = (int) (color1.g * t_ + color2.g * factor), b = (int) (color1.b * t_ + color2.b * factor);
        return new DLColor(a, r, g, b);
    }

    /**
     * Combine two colors using the specified pixel-wise combine mode.
     *
     * <p>The method operates on normalized RGB channels and returns a color with alpha
     * averaged from the inputs (division by 2 via normalized value ((a1+a2)/510f) in implementation).
     * Depending on the mode, different algebra is applied; see {@link CombineMode} for semantics.
     *
     * @param c1 first operand color
     * @param c2 second operand color
     * @param mode the combine algorithm to apply
     * @return new DLColor with combined RGB values and combined alpha as implemented
     * @throws IllegalStateException if either color is {@link #UNDEFINED}
     */
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
        return DLColor.of((c1.a + c2.a) / 510f, resR, resG, resB);
    }

    /**
     * Alpha-composite foreground over background using standard "over" operator.
     *
     * <p>Resulting alpha is fgA + bgA * (1 - fgA). RGB channels are premultiplied during calculation
     * and divided by resulting alpha to produce non-premultiplied output. If resulting alpha is zero,
     * {@link #TRANSPARENT} is returned.
     *
     * @param foreground top layer color
     * @param background bottom layer color
     * @return composited DLColor
     * @throws IllegalStateException if either color is {@link #UNDEFINED}
     */
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

    /**
     * Produce a visually plausible tint between two colors.
     *
     * <p>The method blends channels using a heuristic that emphasizes the larger channel values,
     * creating a tint that tends towards the more dominant channel while blending some of the other.
     * Alpha is averaged.
     *
     * @param colorA first color
     * @param colorB second color
     * @return new DLColor representing the tint
     * @throws IllegalStateException if either color is {@link #UNDEFINED}
     */
    public static DLColor mixTint(DLColor colorA, DLColor colorB) {
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
        return DLColor.of((alphaA + alphaB) / 2f, result[0], result[1], result[2]);
    }

    /**
     * Choose between two colors based on the perceived brightness of a base color.
     *
     * <p>If the base color is considered light (strictly greater than threshold) the method returns
     * {@code darkColor} (suitable for foreground elements), otherwise returns {@code lightColor}.
     *
     * @param base the color used to evaluate brightness
     * @param lightColor color returned for dark bases
     * @param darkColor color returned for light bases
     * @param threshold luminance threshold in [0.0,1.0]
     * @return either darkColor or lightColor depending on base's brightness
     * @throws IllegalStateException if {@code base} is {@link #UNDEFINED}
     */
    public static DLColor pickBasedOnBrightness(DLColor base, DLColor lightColor, DLColor darkColor, float threshold) {
        base.checkDefined();
        return base.isLight(threshold) ? darkColor : lightColor;
    }

    /**
     * Euclidean distance between two colors in RGB 8-bit space.
     *
     * <p>Useful for basic nearest-color or difference tests; this distance does not account for
     * perceptual color difference metrics (like CIEDE2000).
     *
     * @param c1 first color
     * @param c2 second color
     * @return Euclidean distance in RGB space as a double
     * @throws IllegalStateException if either color is {@link #UNDEFINED}
     */
    public static double distance(DLColor c1, DLColor c2) {
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

    /**
     * Equality semantics:
     * <ul>
     *   <li>If either side is {@link #UNDEFINED}, equality is true only if both are the sentinel.</li>
     *   <li>For defined colors equality is true when all four ARGB channels match exactly.</li>
     * </ul>
     *
     * @param o object to compare
     * @return true when equal according to the rules above
     */
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

    /**
     * Hash code for this color.
     *
     * <p>Defined colors return the packed ARGB int. The {@link #UNDEFINED} sentinel returns -1
     * to distinguish it from any valid ARGB value.
     *
     * @return int hash code
     */
    @Override
    public int hashCode() {
        return isDefined ? getAsARGB() : -1;
    }

    /**
     * Human-readable string describing this color or the UNDEFINED sentinel.
     *
     * @return string in the form "DLColor[A=.., R=.., G=.., B=..]" or "DLColor[UNDEFINED]"
     */
    @Override
    public String toString() {
        return isDefined ? String.format("DLColor[A=%d, R=%d, G=%d, B=%d]", a, r, g, b) : "DLColor[UNDEFINED]";
    }

}
