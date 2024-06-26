package de.mrjulsen.mcdragonlib.neoforge;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.neoforged.fml.common.Mod;


@Mod(DragonLib.MODID)
public final class DragonLibNeoForge {
    public DragonLibNeoForge() {
        DragonLib.init();
    }
}
