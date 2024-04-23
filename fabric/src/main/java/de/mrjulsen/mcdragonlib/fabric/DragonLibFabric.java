package de.mrjulsen.mcdragonlib.fabric;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.fabricmc.api.ModInitializer;

public class DragonLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DragonLib.init();
    }
}
