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
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

public enum EnvSide {
    SERVER((byte)0, "server"),
    CLIENT((byte)1, "client"),
    BOTH((byte)99, "both");

    byte index;
    String name;

    EnvSide(byte index, String name) {
        this.index = index;
        this.name = name;
    }

    public byte getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public static EnvSide getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(SERVER);
    }

    public static EnvSide current() {
        boolean hasServer = DragonLib.hasServer();
        boolean env = Platform.getEnv() == EnvType.CLIENT;
        return hasServer && env ? BOTH : (hasServer ? SERVER : CLIENT);
    }
}
