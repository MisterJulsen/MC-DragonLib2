package de.mrjulsen.mcdragonlib.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

import de.mrjulsen.mcdragonlib.ExampleExpectPlatform;

public class ExampleExpectPlatformImpl {
    /**
     * This is our actual method to {@link ExampleExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
