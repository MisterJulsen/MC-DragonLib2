package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

import java.util.Arrays;

public final class MaskTextTransform implements ITextTransform {

    public static final char BULLET = '●';
    public static final char ASTERISK = '*';

    private final char mask;

    public MaskTextTransform(char mask) {
        this.mask = mask;
    }

    public char mask() {
        return mask;
    }

    @Override
    public String apply(int line, String text) {
        if (text.isEmpty()) {
            return text;
        }
        char[] masked = new char[text.length()];
        Arrays.fill(masked, mask);
        return new String(masked);
    }
}
