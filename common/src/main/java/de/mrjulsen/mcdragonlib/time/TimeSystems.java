package de.mrjulsen.mcdragonlib.time;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.config.ModServerConfig;
import de.mrjulsen.mcdragonlib.time.internal.TimeSystemState;
import dev.architectury.platform.Platform;

/**
 * Decides which {@link TimeSystem} is in use, and lets other mods provide one.
 *
 * <h2>Which system is used</h2>
 * <p>The first match wins:</p>
 * <ol>
 * <li>On a client connected to a server: the system the server uses. The server is the authority, so
 *     both sides always calculate with the same system.</li>
 * <li>A datapack system, from {@code data/dragonlib/config/time_system.json}.</li>
 * <li>A system provided by a time changing mod, see {@link #registerProvider(String, Supplier)}, if
 *     automatic detection is enabled in the server config.</li>
 * <li>The custom system from the config, if enabled there.</li>
 * <li>{@link TimeSystem#VANILLA}.</li>
 * </ol>
 * <p>The result is cached for a short time (configurable), so calling {@link #current()} often is cheap.</p>
 */
public final class TimeSystems {

    private static final Map<String, Supplier<TimeSystem>> PROVIDERS = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Set<String> FAILED_PROVIDERS = ConcurrentHashMap.newKeySet();

    private static volatile TimeSystem cached;
    private static volatile long cachedAt;

    private TimeSystems() {}

    /**
     * The time system currently in use. Never null.
     */
    public static TimeSystem current() {
        if (!DragonLib.hasServer()) {
            TimeSystem synced = TimeSystemState.synced();
            if (synced != null) {
                return synced;
            }
        }
        return resolveLocal();
    }

    /**
     * The section of the day the overworld is currently in.
     */
    public static TimeSection currentSection() {
        return GameTime.now().section();
    }

    /**
     * Lets a time changing mod tell DragonLib how fast time runs. The supplier is only asked while that
     * mod is loaded, and again whenever the cache expires, so it may return a different system as the
     * mod's settings change. Returning null means "no opinion".
     * <p>If several such mods are present, the one configured as preferred in the server config wins,
     * otherwise the one registered first.</p>
     *
     * @param modId the id of the time changing mod
     * @param provider builds the time system matching that mod's current settings
     */
    public static void registerProvider(String modId, Supplier<TimeSystem> provider) {
        PROVIDERS.put(Objects.requireNonNull(modId, "modId"), Objects.requireNonNull(provider, "provider"));
        invalidate();
    }

    public static boolean hasProvider(String modId) {
        return PROVIDERS.containsKey(modId);
    }

    /**
     * Drops the cached system, so the next {@link #current()} resolves it again.
     */
    public static void invalidate() {
        cached = null;
    }

    /**
     * Resolves the system from this side's own sources, ignoring what a server sent.
     */
    public static TimeSystem resolveLocal() {
        TimeSystem result = cached;
        long ttlMillis = cacheTtlMillis();
        long now = System.nanoTime();
        if (result != null && now - cachedAt <= TimeUnit.MILLISECONDS.toNanos(ttlMillis)) {
            return result;
        }
        result = resolveUncached();
        cached = result;
        cachedAt = now;
        return result;
    }

    private static TimeSystem resolveUncached() {
        TimeSystem datapack = TimeSystemState.datapack();
        if (datapack != null) {
            return datapack;
        }
        if (config(ModServerConfig.AUTO_DETECT_TIME_SYSTEM::get, true)) {
            Optional<TimeSystem> provided = fromProvider();
            if (provided.isPresent()) {
                return provided.get();
            }
        }
        if (config(ModServerConfig.USE_CUSTOM::get, false)) {
            return fromConfig().orElse(TimeSystem.VANILLA);
        }
        return TimeSystem.VANILLA;
    }

    private static Optional<TimeSystem> fromProvider() {
        String preferred = config(ModServerConfig.PREFERRED_MOD::get, "").trim();
        Map<String, Supplier<TimeSystem>> providers;
        synchronized (PROVIDERS) {
            providers = new LinkedHashMap<>(PROVIDERS);
        }
        for (Map.Entry<String, Supplier<TimeSystem>> entry : providers.entrySet()) {
            String modId = entry.getKey();
            if (!Platform.isModLoaded(modId) || (!preferred.isEmpty() && !modId.equalsIgnoreCase(preferred))) {
                continue;
            }
            try {
                TimeSystem system = entry.getValue().get();
                FAILED_PROVIDERS.remove(modId);
                return Optional.ofNullable(system);
            } catch (RuntimeException e) {
                if (FAILED_PROVIDERS.add(modId)) {
                    DragonLib.LOGGER.error("The time system provider of '{}' failed, falling back to the next source.", modId, e);
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<TimeSystem> fromConfig() {
        try {
            long ticksPerDay = ModCommonConfig.TIME_TICKS_PER_DAY.get();
            return Optional.of(TimeSystem.uniform(ticksPerDay, ModCommonConfig.TIME_DEFAULT_TPS.get(), ticksPerDay * ModCommonConfig.TIME_DAYTIME_SHIFT_FACTOR.get()));
        } catch (IllegalStateException e) {
            return Optional.empty(); // Config not loaded yet.
        } catch (IllegalArgumentException e) {
            DragonLib.LOGGER.error("The custom time system in the config is invalid, using vanilla instead: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static long cacheTtlMillis() {
        return config(ModCommonConfig.TIME_SYSTEM_CACHE_TTL::get, 100);
    }

    private static <T> T config(Supplier<T> value, T fallback) {
        try {
            T result = value.get();
            return result == null ? fallback : result;
        } catch (IllegalStateException e) {
            return fallback;
        }
    }
}
