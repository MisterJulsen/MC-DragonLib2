package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

@FunctionalInterface
public interface ILineAlignment {
    ETextAlignment alignmentOf(CharSequence line, int from, int to);
}
