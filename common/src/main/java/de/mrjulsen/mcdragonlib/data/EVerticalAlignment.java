package de.mrjulsen.mcdragonlib.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.DragonLib;

public enum EVerticalAlignment implements ITranslatableEnum {
    TOP(0, "top"),
    CENTER(1, "center"),
    BOTTOM(2, "bottom");

    private static final String ENUM_NAME = "vertical_alignment";

    private final int id;
    private final String name;

    private EVerticalAlignment(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float offset(float available, float content) {
        return switch (this) {
            case CENTER -> (available - content) / 2.0F;
            case BOTTOM -> available - content;
            default -> 0.0F;
        };
    }

    public static EVerticalAlignment getById(int id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(TOP);
    }

    @Override
    public Data getTranslationData() {
        return new Data(DragonLib.MODID, ENUM_NAME, getName());
    }
}
