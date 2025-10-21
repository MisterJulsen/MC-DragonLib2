package de.mrjulsen.mcdragonlib.util.time;

import java.util.List;

public class ConfigTimeSystem implements ITimeSystem {
    private static final List<TimeZone> MODDED_ZONES = List.of(
        new TimeZone(0, 12000L, 12000D / 720D), // Tag
        new TimeZone(12000L, 24000L, 12000D / 180D) // Nacht
    );
    
    @Override
    public long getTicksPerDay() {
        return 24000L;
    }

    @Override
    public List<TimeZone> getTimeZones() {
        return MODDED_ZONES;
    }
}
