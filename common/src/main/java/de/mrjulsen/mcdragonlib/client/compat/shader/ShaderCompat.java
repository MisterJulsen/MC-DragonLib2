package de.mrjulsen.mcdragonlib.client.compat.shader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.platform.Platform;

public final class ShaderCompat {
    private ShaderCompat() {}

    private static final Map<String, Supplier<IShaderPackDetector>> registered = new HashMap<>();
    private static final List<IShaderPackDetector> compiledDetectors = new ArrayList<>();
    private static boolean initialized;

    static {
        register("iris", IrisShaderPackDetector::new);
        register("oculus", IrisShaderPackDetector::new);
    }

    private static void init() {
        initialized = true;
        compiledDetectors.clear();

        for (Map.Entry<String, Supplier<IShaderPackDetector>> entry : registered.entrySet()) {
            if (Platform.isModLoaded(entry.getKey())) {
                try {
                    compiledDetectors.add(entry.getValue().get());
                } catch (Throwable e) {
                    DragonLib.LOGGER.warn("Unable to initialize shader pack detection.", e);
                }
            }
        }
        registered.clear();
    }

    public static void register(String modid, Supplier<IShaderPackDetector> factory) {
        if (initialized) {
            throw new IllegalStateException("Unable to register new shader pack detectors while the game is already running.");
        }
        registered.put(modid, factory);
    }

    public static boolean isShaderPackActive() {
        if (!initialized) {
            init();
        }

        for (int i = 0; i < compiledDetectors.size(); i++) {
            IShaderPackDetector detector = compiledDetectors.get(i);
            try {
                if (detector.isShaderPackInUse()) {
                    return true;
                }
            } catch (Throwable e) {
                compiledDetectors.remove(i--);
                DragonLib.LOGGER.warn("Shader pack detector " + detector.getClass().getSimpleName() + " failed and has been disabled.", e);
            }
        }
        return false;
    }
}
