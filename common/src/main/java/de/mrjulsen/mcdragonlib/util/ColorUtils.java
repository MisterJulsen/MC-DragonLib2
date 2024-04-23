package de.mrjulsen.mcdragonlib.util;

public final class ColorUtils {
    
    public static int applyTint(int color, int tint) {
        int originalRed = (color >> 16) & 0xFF;
        int originalGreen = (color >> 8) & 0xFF;
        int originalBlue = color & 0xFF;

        int tintRed = (tint >> 16) & 0xFF;
        int tintGreen = (tint >> 8) & 0xFF;
        int tintBlue = tint & 0xFF;

        int mixedRed = (originalRed + tintRed) / 2;
        int mixedGreen = (originalGreen + tintGreen) / 2;
        int mixedBlue = (originalBlue + tintBlue) / 2;

        return 0xFF000000 | (mixedRed << 16) | (mixedGreen << 8) | mixedBlue;
    }

	public static int lightenColor(int color, float fac) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        red = (int) ((red * (1 - fac)) + (fac < 0 ? 0 : (255 * fac)));
        green = (int) ((green * (1 - fac)) + (fac < 0 ? 0 : (255 * fac)));
        blue = (int) ((blue * (1 - fac)) + (fac < 0 ? 0 : (255 * fac)));

        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

	public static int darkenColor(int color, float fac) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int r = (int) (red * (1 - fac));
        int g = (int) (green * (1 - fac));
        int b = (int) (blue * (1 - fac));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static short[] decodeARGB(int color) {
        short a = (short)((color >> 24) & 0xFF);
        short r = (short)((color >> 16) & 0xFF);
        short g = (short)((color >> 8) & 0xFF);
        short b = (short)(color & 0xFF);

        return new short[] {a, r, g, b};
    }

    public static int swapRedBlue(int argbColor) {
        int alpha = (argbColor >> 24) & 0xFF;
        int red = (argbColor >> 16) & 0xFF;
        int green = (argbColor >> 8) & 0xFF;
        int blue = argbColor & 0xFF;
    
        int bgrColor = (alpha << 24) | (blue << 16) | (green << 8) | red;
    
        return bgrColor;
    }

    public static int argb(int a, int r, int g, int b) {
        int vA = (a & 0xFF) << 24; 
        int vR = (r & 0xFF) << 16;
        int vG = (g & 0xFF) << 8;
        int vB = (b & 0xFF);
    
        return vA | vR | vG | vB;
    }
    
}
