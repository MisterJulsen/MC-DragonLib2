package de.mrjulsen.mcdragonlib.fabric;

import fuzs.forgeconfigapiport.fabric.impl.core.NeoForgeConfigRegistryImpl;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.config.ModServerConfig;
import net.neoforged.fml.config.ModConfig;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {
        NeoForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
        NeoForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.SERVER, ModServerConfig.SPEC, DragonLib.MODID + "-server.toml");
    }
}
