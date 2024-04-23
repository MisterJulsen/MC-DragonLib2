package de.mrjulsen.mcdragonlib.core;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraft.util.StringRepresentable;

public enum EAlignment implements StringRepresentable, ITranslatableEnum {
    LEFT(0, "left"),
    CENTER(1, "center"),
    RIGHT(2, "right");

    private int id;
    private String name;

    private EAlignment(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static EAlignment getById(int id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(LEFT);
    }

    @Override
    public String getEnumName() {
        return "alignment";
    }

    @Override
    public String getEnumValueName() {
        return getName();
    }

    @Override
    public String getSerializedName() {
        return getValueTranslationKey(DragonLib.MODID);
    }


}
