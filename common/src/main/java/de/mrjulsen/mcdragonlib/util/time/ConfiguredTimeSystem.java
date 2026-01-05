package de.mrjulsen.mcdragonlib.util.time;

import java.util.List;

import de.mrjulsen.mcdragonlib.config.ModCommonConfig;

/**
 * Time system built from mod configuration values.
 *
 * <p>It uses configuration entries for ticks-per-day, default TPS and a daytime shift factor.
 * Exposes a singleton {@link #INSTANCE}.
 */
public class ConfiguredTimeSystem implements ITimeSystem {

    public static final ConfiguredTimeSystem INSTANCE = new ConfiguredTimeSystem();

    @Deprecated(forRemoval = true)
    public ConfiguredTimeSystem() {
    }
    
    @Override
    public long getTicksPerDay() {
        return ModCommonConfig.TIME_TICKS_PER_DAY.get();
    }

    @Override
    public List<TimeZone> getTimeZones() {
        return List.of(new TimeZone(0, getTicksPerDay(), ModCommonConfig.TIME_DEFAULT_TPS.get()));
    }

    @Override
    public double getDaytimeOffset() {
        return (double)getTicksPerDay() * ModCommonConfig.TIME_DAYTIME_SHIFT_FACTOR.get();
    }
}
