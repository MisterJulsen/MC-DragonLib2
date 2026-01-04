package de.mrjulsen.mcdragonlib.config;

import net.minecraft.util.StringRepresentable;

public enum ETimeSystem implements StringRepresentable {
    AUTO(0, "auto"),
    VANILLA(1, "vanilla"),
    CUSTOM(2, "custom");

    private final int id;
    private final String name;

    private ETimeSystem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
