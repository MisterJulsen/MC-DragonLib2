package de.mrjulsen.mcdragonlib.forge;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.config.ModServerConfig;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ModServerConfig.SPEC, DragonLib.MODID + "-server.toml");
    }
}
