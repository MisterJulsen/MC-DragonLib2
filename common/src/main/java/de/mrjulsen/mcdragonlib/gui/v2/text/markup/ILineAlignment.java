package de.mrjulsen.mcdragonlib.gui.v2.text.markup;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

@FunctionalInterface
public interface ILineAlignment {
    ETextAlignment alignmentOf(CharSequence line, int from, int to);
}
