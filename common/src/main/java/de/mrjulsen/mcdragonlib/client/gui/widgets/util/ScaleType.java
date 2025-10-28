package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

import java.util.Arrays;

public enum ScaleType {
    STRETCH("stretch"),
    TILE("tile"),
    NINE_SLICE("nine_slice");

    private final String name;

    private ScaleType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ScaleType getByName(String name) {
        return Arrays.stream(values()).filter(x -> x.getName().equals(name)).findFirst().orElse(STRETCH);
    }
    
}
