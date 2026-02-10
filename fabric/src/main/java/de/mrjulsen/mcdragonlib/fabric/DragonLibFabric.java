package de.mrjulsen.mcdragonlib.fabric;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class DragonLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DragonLib.init();
    }




}

