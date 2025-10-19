package de.mrjulsen.mcdragonlib.core;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.DragonLib;

public enum ETextAlignment implements ITranslatableEnum {
    LEFT(0, "left"),
    CENTER(1, "center"),
    RIGHT(2, "right");

    private static final String ENUM_NAME = "alignment";

    private int id;
    private String name;

    private ETextAlignment(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static ETextAlignment getById(int id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(LEFT);
    }

    @Override
    public Data getTranslationData() {
        return new Data(DragonLib.MODID, ENUM_NAME, getName());
    }


}
