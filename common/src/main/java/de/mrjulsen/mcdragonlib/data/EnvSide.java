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
