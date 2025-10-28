package de.mrjulsen.mcdragonlib.forge;

import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DragonLib.MODID)
public class DragonLibForge {
    public DragonLibForge() {
        EventBuses.registerModEventBus(DragonLib.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        DragonLib.init();
    }
}
