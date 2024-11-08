package de.mrjulsen.mcdragonlib.config;

import java.util.Arrays;

import net.minecraft.util.StringRepresentable;

public enum ECachingMode implements StringRepresentable {
    NORMAL(0, "normal"),
    REDUCED(1, "reduced"),
    MINIMAL(2, "minimal"),
    OFF(3, "off");

    int idx;
    String name;

    ECachingMode(int idx, String name) {
        this.idx = idx;
        this.name = name;
    }

    public int getIndex() {
        return idx;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static ECachingMode getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(NORMAL);
    }
}
