package de.mrjulsen.mcdragonlib.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

import de.mrjulsen.mcdragonlib.ExampleExpectPlatform;

public class ExampleExpectPlatformImpl {
    /**
     * This is our actual method to {@link ExampleExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
