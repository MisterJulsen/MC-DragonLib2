package de.mrjulsen.mcdragonlib.util.time;

import java.util.List;

/**
 * Default time system implementing the vanilla Minecraft behaviour: a single zone spanning the whole day at 20 TPS.
 *
 * <p>Provides a singleton {@link #INSTANCE} for consumers that want the standard mapping.
 */
public final class VanillaTimeSystem implements ITimeSystem {

    public static final VanillaTimeSystem INSTANCE = new VanillaTimeSystem();

    private static final List<TimeZone> VANILLA_ZONES = List.of(
        new TimeZone(0, 24000L, 20D)
    );

    private VanillaTimeSystem() {
    }
    
    @Override
    public long getTicksPerDay() {
        return 24000L;
    }

    @Override
    public List<TimeZone> getTimeZones() {
        return VANILLA_ZONES;
    }
}
