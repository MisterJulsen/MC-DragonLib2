package de.mrjulsen.mcdragonlib.util.time;

/**
 * Describes a contiguous segment of the in-game day with a specific ticks-per-second rate.
 *
 * @param startTick inclusive start tick index {@code >= 0}
 * @param endTick exclusive end tick index ({@code <= ticksPerDay} for the system)
 * @param tps ticks-per-second value used inside this zone (must be {@code > 0})
 */
public record TimeZone(long startTick, long endTick, double tps) implements Comparable<TimeZone> {
    public TimeZone {
        if (startTick < 0 || endTick < 0) {
            throw new IllegalArgumentException("Ticks must be non-negative.");
        }
    }

    @Override
    public int compareTo(TimeZone o) {
        return Long.compare(this.startTick, o.startTick);
    }

    /**
     * Return the real-world seconds duration of a single tick in this zone.
     *
     * @return seconds per tick ({@code 1.0 / tps})
     */
    public double getRealSecondsPerTick() {
        return 1.0 / tps;
    }

    /**
     * Return the real-world milliseconds duration of a single tick in this zone.
     *
     * @return milliseconds per tick ({@code secondsPerTick * 1000})
     */
    public double getRealMillisPerTick() {
        return getRealSecondsPerTick() * 1000.0;
    }
}
