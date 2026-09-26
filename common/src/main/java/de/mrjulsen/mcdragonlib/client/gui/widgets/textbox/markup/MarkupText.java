package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

public final class MarkupText {

    private MarkupText() {
    }

    public static int dimmed(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static TextStyle markupStyle(int color) {
        return TextStyle.DEFAULT.withColor(color).with(StyleFlag.MARKUP, true);
    }

    public static StyledSpan markup(int start, int end, int color) {
        return new StyledSpan(start, end, markupStyle(color));
    }

    public static int indentOf(CharSequence line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

    public static int runLength(CharSequence line, int start, char c) {
        int i = start;
        while (i < line.length() && line.charAt(i) == c) {
            i++;
        }
        return i - start;
    }

    public static boolean startsWith(CharSequence line, int at, String text) {
        if (at < 0 || at + text.length() > line.length()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (line.charAt(at + i) != text.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEscaped(CharSequence line, int index) {
        int backslashes = 0;
        for (int i = index - 1; i >= 0 && line.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return (backslashes & 1) == 1;
    }

    public static int indexOfUnescaped(CharSequence line, int from, int to, char c) {
        for (int i = from; i < to; i++) {
            if (line.charAt(i) == c && !isEscaped(line, i)) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOfUnescaped(CharSequence line, int from, int to, String text) {
        int limit = to - text.length();
        for (int i = from; i <= limit; i++) {
            if (startsWith(line, i, text) && !isEscaped(line, i)) {
                return i;
            }
        }
        return -1;
    }

    public static int matching(CharSequence line, int open, int to, char opening, char closing) {
        int depth = 0;
        for (int i = open; i < to; i++) {
            if (isEscaped(line, i)) {
                continue;
            }
            char c = line.charAt(i);
            if (c == opening) {
                depth++;
            } else if (c == closing) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int parseInt(CharSequence line, int from, int to, int limit) {
        int value = 0;
        for (int i = from; i < to && value < limit; i++) {
            value = value * 10 + (line.charAt(i) - '0');
        }
        return value;
    }
}
