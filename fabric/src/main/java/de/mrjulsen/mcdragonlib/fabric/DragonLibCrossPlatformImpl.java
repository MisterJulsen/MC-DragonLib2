package de.mrjulsen.mcdragonlib.fabric;

import net.minecraftforge.fml.config.ModConfig;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import fuzs.forgeconfigapiport.impl.config.ForgeConfigRegistryImpl;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {
        ForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
    }
}
