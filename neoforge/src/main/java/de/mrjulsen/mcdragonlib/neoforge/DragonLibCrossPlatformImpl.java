package de.mrjulsen.mcdragonlib.neoforge;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import net.neoforged.fml.config.ModConfig;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {
        DragonLibNeoForge.getModContainer().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
    }
}
