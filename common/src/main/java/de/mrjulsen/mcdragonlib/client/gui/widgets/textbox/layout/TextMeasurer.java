package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;

public final class TextMeasurer {

    private static final float MIN_SCALE = 0.01F;

    private final Font font;

    public TextMeasurer(Font font) {
        this.font = font;
    }

    public static TextMeasurer ofDefaultFont() {
        return new TextMeasurer(Minecraft.getInstance().font);
    }

    public Font font() {
        return font;
    }

    public float baseLineHeight() {
        return font.lineHeight;
    }

    public float lineHeight(TextStyle style) {
        return font.lineHeight * style.scale();
    }

    public float width(CharSequence text, int from, int to, TextStyle style) {
        if (from >= to) {
            return 0.0F;
        }
        return width(text.subSequence(from, to).toString(), style);
    }

    public float width(String text, TextStyle style) {
        if (text.isEmpty()) {
            return 0.0F;
        }
        return font.getSplitter().stringWidth(FormattedText.of(text, style.toVanillaStyle())) * style.scale();
    }

    public int fitCharacters(String text, TextStyle style, float maxWidth) {
        if (text.isEmpty() || maxWidth <= 0.0F) {
            return 0;
        }
        int scaledWidth = (int) Math.floor(maxWidth / Math.max(style.scale(), MIN_SCALE));
        return font.getSplitter().plainIndexAtWidth(text, scaledWidth, style.toVanillaStyle());
    }

    public int indexAtWidth(String text, TextStyle style, float targetX) {
        if (text.isEmpty() || targetX <= 0.0F) {
            return 0;
        }
        int index = snapToCodePoint(text, fitCharacters(text, style, targetX));
        if (index >= text.length()) {
            return text.length();
        }

        int next = index + Character.charCount(text.codePointAt(index));
        float before = width(text.substring(0, index), style);
        float after = width(text.substring(0, next), style);
        return (targetX - before) > (after - targetX) ? next : index;
    }

    public static int snapToCodePoint(String text, int index) {
        if (index > 0 && index < text.length()
                && Character.isLowSurrogate(text.charAt(index))
                && Character.isHighSurrogate(text.charAt(index - 1))) {
            return index - 1;
        }
        return index;
    }
}
