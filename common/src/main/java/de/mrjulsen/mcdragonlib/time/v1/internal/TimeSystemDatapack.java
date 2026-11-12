package de.mrjulsen.mcdragonlib.time.v1.internal;

import java.io.Reader;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.time.v1.TimeSystem;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Loads the time system from {@code data/dragonlib/config/time_system.json}.
 * <p>Example:</p>
 * <pre>{@code
 * {
 *   "ticks_per_day": 24000,   // optional, default 24000
 *   "daytime_offset": 6000,   // optional, ticks the clock shows at tick 0, default ticks_per_day / 4 (06:00)
 *   "zones": [   // optional, uncovered parts run at 20 ticks per second
 *     { "start_tick": 0, "end_tick": 12000, "tps": 20 },
 *     { "start_tick": 12000, "end_tick": 24000, "tps": 40 }
 *   ]
 * }
 * }</pre>
 */
public final class TimeSystemDatapack extends SimplePreparableReloadListener<Optional<TimeSystem>> {

    public static final ResourceLocation LOCATION = DLUtils.resourceLocation(DragonLib.MODID, "config/time_system.json");

    private TimeSystemDatapack() {}

    public static void register() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new TimeSystemDatapack());
    }

    @Override
    protected Optional<TimeSystem> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Optional<Resource> resource = resourceManager.getResource(LOCATION);
        if (resource.isEmpty()) {
            return Optional.empty();
        }
        try (Reader reader = resource.get().openAsReader()) {
            return Optional.of(parse(GsonHelper.parse(reader)));
        } catch (Exception e) {
            DragonLib.LOGGER.error("The time system in {} is invalid and was ignored.", LOCATION, e);
            return Optional.empty();
        }
    }

    @Override
    protected void apply(Optional<TimeSystem> system, ResourceManager resourceManager, ProfilerFiller profiler) {
        TimeSystemState.setDatapack(system.orElse(null));
        system.ifPresent(s -> DragonLib.LOGGER.info("Loaded the time system from a datapack: {}", s));
    }

    static TimeSystem parse(JsonObject json) {
        long ticksPerDay = GsonHelper.getAsLong(json, "ticks_per_day", TimeSystem.VANILLA_TICKS_PER_DAY);
        TimeSystem.Builder builder = TimeSystem.builder(ticksPerDay).clockOffset(GsonHelper.getAsDouble(json, "daytime_offset", ticksPerDay / 4.0));
        if (json.has("zones")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "zones")) {
                JsonObject zone = GsonHelper.convertToJsonObject(element, "zone");
                builder.section(GsonHelper.getAsLong(zone, "start_tick"), GsonHelper.getAsLong(zone, "end_tick"), GsonHelper.getAsDouble(zone, "tps"));
            }
        }
        return builder.build();
    }
}
