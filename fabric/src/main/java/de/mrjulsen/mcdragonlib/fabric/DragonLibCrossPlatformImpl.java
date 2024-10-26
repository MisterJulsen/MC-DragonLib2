package de.mrjulsen.mcdragonlib.fabric;

import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {
        ModLoadingContext.registerConfig(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
    }
}
