package de.mrjulsen.mcdragonlib.neoforge;

import net.neoforged.fml.common.Mod;
import de.mrjulsen.mcdragonlib.DragonLib;

@Mod(DragonLib.MODID)
public final class DragonLibNeoForge {
    public DragonLibNeoForge() {
        DragonLib.init();
    }
}
