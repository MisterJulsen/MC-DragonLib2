package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

public enum RenderLayer {
    BACK(500, false),
    MAIN(0, false),
    FRONT(-1000, false),
    SCREEN_SPACE(-1500, true),
    OVERLAY(-2000, true);

    private final int z;
    private final boolean special;

    private RenderLayer(int z, boolean special) {
        this.z = z;
        this.special = special;
    }

    public int z() {
        return z;
    }

    public boolean isSpecial() {
        return special;
    }

}
