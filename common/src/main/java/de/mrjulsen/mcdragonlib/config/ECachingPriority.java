package de.mrjulsen.mcdragonlib.config;

import java.util.Arrays;

import net.minecraft.util.StringRepresentable;

/** The importance of caching data. */
public enum ECachingPriority implements StringRepresentable {
    /** Must always be cached, regardless of the settings the user has made. */
    ALWAYS(1, "always"),
    NORMAL(0, "normal"),
    LOW(-1, "low"),
    LOWEST(-2, "lowest");

    int idx;
    String name;

    ECachingPriority(int idx, String name) {
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

    public static ECachingPriority getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(NORMAL);
    }

    public boolean shouldCache() {
        return getIndex() >= LOWEST.getIndex() + (ModCommonConfig.SPEC.isLoaded() ? ModCommonConfig.CACHING.get().getIndex() : ECachingPriority.NORMAL.getIndex());
    }
}
