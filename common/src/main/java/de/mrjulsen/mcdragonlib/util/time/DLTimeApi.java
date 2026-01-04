package de.mrjulsen.mcdragonlib.util.time;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.config.ModServerConfig;
import dev.architectury.platform.Platform;

/**
 * Small registry used to register mod compatibility time systems and to resolve a preferred/current one.
 *
 * <p>Other mods can call {@link #registerCompat(String, Supplier)} to supply an ITimeSystem when their mod is present.
 * The API can then choose a preferred compat implementation based on loaded mods and server preferences.
 */
public final class DLTimeApi {
    private DLTimeApi() {}

    private static final Map<String, Supplier<ITimeSystem>> compatTimeSystems = new ConcurrentHashMap<>();

    /**
     * Register a compatibility time system supplier for a mod id.
     *
     * @param modid mod id the supplier is associated with
     * @param timeSystem supplier producing an ITimeSystem (may return null)
     */
    public static void registerCompat(String modid, Supplier<ITimeSystem> timeSystem) {
        compatTimeSystems.put(modid, timeSystem);
    }

    /**
     * Check whether a compat time system was registered for the given mod id.
     *
     * @param modid mod id to check
     * @return true if a supplier was registered
     */
    public static boolean hasCompatForMod(String modid) {
        return compatTimeSystems.containsKey(modid);
    }

    /**
     * Attempt to resolve a current compatibility time system based on loaded mods and server preference.
     *
     * <p>This method checks {@link ModServerConfig#PREFERRED_MOD} and the platform mod presence to choose a supplier.
     *
     * @return Optional containing a resolved ITimeSystem or empty if none matched
     */
    static Optional<ITimeSystem> getCurrentCompat() {
        String preferredMod = ModServerConfig.PREFERRED_MOD.get();
        boolean hasPreference = !preferredMod.isBlank();
        for (Map.Entry<String, Supplier<ITimeSystem>> pair : compatTimeSystems.entrySet()) {
            if (Platform.isModLoaded(pair.getKey()) && (!hasPreference || pair.getKey().toLowerCase().equals(preferredMod.toLowerCase()))) {
                return Optional.ofNullable(pair.getValue().get());
            }
        }
        return Optional.empty();
    }
    
}
