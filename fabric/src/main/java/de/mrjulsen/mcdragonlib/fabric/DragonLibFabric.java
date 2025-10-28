package de.mrjulsen.mcdragonlib.fabric;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.network.fabric.DLNetworkManagerImpl;
import net.fabricmc.api.ModInitializer;

public class DragonLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DragonLib.init();
        DLNetworkManagerImpl.init();
    }
}
