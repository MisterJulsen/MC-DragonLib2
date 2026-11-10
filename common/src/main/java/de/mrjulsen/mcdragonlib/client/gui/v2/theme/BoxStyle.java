package de.mrjulsen.mcdragonlib.client.gui.v2.theme;

import de.mrjulsen.mcdragonlib.util.DLColor;

public record BoxStyle(DLColor border, DLColor fill, int thickness) {

    public BoxStyle(DLColor border, DLColor fill) {
        this(border, fill, 1);
    }

    public BoxStyle(DLColor all, int thickness) {
        this(all, all, thickness);
    }

    public BoxStyle(DLColor all) {
        this(all, 1);
    }
}
