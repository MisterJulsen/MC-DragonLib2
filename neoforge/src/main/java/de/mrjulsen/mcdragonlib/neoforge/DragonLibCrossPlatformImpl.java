package de.mrjulsen.mcdragonlib.neoforge;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import fuzs.forgeconfigapiport.neoforge.impl.forge.ForgeConfigRegistryImpl;
import net.neoforged.fml.config.ModConfig;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {
        ForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
    }
}
