package de.mrjulsen.mcdragonlib.neoforge;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(DragonLib.MODID)
public final class DragonLibNeoForge {

    private static ModContainer container;

    public DragonLibNeoForge(ModContainer cont) {
        container = cont;
        DragonLib.init();
    }

    public static ModContainer getModContainer() {
        return container;
    }
}
