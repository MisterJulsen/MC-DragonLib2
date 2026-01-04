package de.mrjulsen.mcdragonlib.util.time;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Time system constructed from a datapack JSON definition.
 *
 * <p>The JSON may define ticks_per_day, daytime_offset and an array of zones.
 * This class validates ranges and normalises the zones into an immutable list.
 */
public final class DatapackTimeSystem implements ITimeSystem {

    private final long ticksPerDay;
    private final long daytimeOffset;
    private final List<TimeZone> timeZones;

    private DatapackTimeSystem(long ticksPerDay, long daytimeOffset, List<TimeZone> zones) {
        this.ticksPerDay = ticksPerDay;
        this.daytimeOffset = daytimeOffset;
        this.timeZones = List.copyOf(zones);
    }

    /**
     * Parse a DatapackTimeSystem from a JSON object.
     *
     * <p>Provides defaults when fields are missing and ensures there is at least one zone.
     *
     * @param json JSON object with optional fields
     * @return parsed DatapackTimeSystem
     * @throws IllegalArgumentException if zones are invalid
     */
    public static DatapackTimeSystem fromJson(JsonObject json) {
        long ticksPerDay = json.has("ticks_per_day")
                ? json.get("ticks_per_day").getAsLong()
                : 24000L;

        long offset = json.has("daytime_offset")
                ? json.get("daytime_offset").getAsLong()
                : 0L;

        List<TimeZone> zones = new ArrayList<>();

        if (json.has("zones")) {
            JsonArray arr = json.getAsJsonArray("zones");

            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();

                long start = o.get("start_tick").getAsLong();
                long end = o.get("end_tick").getAsLong();
                double tps = o.get("tps").getAsDouble();

                zones.add(new TimeZone(start, end, tps));
            }
        }

        if (zones.isEmpty()) {
            zones.add(new TimeZone(0, ticksPerDay, 20.0));
        }

        validate(zones, ticksPerDay);

        return new DatapackTimeSystem(ticksPerDay, offset, zones);
    }

    private static void validate(List<TimeZone> zones, long ticksPerDay) {
        for (TimeZone z : zones) {
            if (z.startTick() < 0 || z.endTick() > ticksPerDay) {
                throw new IllegalArgumentException("TimeZone out of range: " + z);
            }
            if (z.tps() <= 0) {
                throw new IllegalArgumentException("Invalid TPS in TimeZone: " + z);
            }
        }
    }

    @Override
    public long getTicksPerDay() {
        return ticksPerDay;
    }

    @Override
    public double getDaytimeOffset() {
        return daytimeOffset;
    }

    @Override
    public List<TimeZone> getTimeZones() {
        return timeZones;
    }
    
    @Override
    public String toString() {
        return "DatapackTimeSystem{ticksPerDay=" + ticksPerDay + ", daytimeOffset=" + daytimeOffset + ", zones=" + timeZones + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticksPerDay, daytimeOffset, timeZones);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DatapackTimeSystem other)) return false;
        return ticksPerDay == other.ticksPerDay
                && daytimeOffset == other.daytimeOffset
                && timeZones.equals(other.timeZones);
    }
}

