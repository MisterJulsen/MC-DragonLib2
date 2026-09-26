package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

public record StyledSpan(int start, int end, TextStyle style) {

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return start >= end;
    }

    public boolean isMarkup() {
        return StyleFlag.MARKUP.isSet(style.flags());
    }

    public StyledSpan sub(int from, int to) {
        int s = Math.max(start, from);
        int e = Math.min(end, to);
        return s < e ? new StyledSpan(s, e, style) : null;
    }
}
