/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
