/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
