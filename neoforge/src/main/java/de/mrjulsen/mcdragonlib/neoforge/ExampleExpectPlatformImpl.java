package de.mrjulsen.mcdragonlib.neoforge;

import java.nio.file.Path;

import de.mrjulsen.mcdragonlib.ExampleExpectPlatform;
import net.neoforged.fml.loading.FMLPaths;

public class ExampleExpectPlatformImpl {
    /**
     * This is our actual method to {@link ExampleExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
