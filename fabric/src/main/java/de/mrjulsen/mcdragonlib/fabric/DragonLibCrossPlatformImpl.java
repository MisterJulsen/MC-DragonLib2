package de.mrjulsen.mcdragonlib.fabric;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import fuzs.forgeconfigapiport.fabric.impl.core.NeoForgeConfigRegistryImpl;
import net.neoforged.fml.config.ModConfig.Type;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {        
        NeoForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
    }
}
