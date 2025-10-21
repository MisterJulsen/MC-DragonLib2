package de.mrjulsen.mcdragonlib.util.time;

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

    public double getRealSecondsPerTick() {
        return 1.0 / tps;
    }

    public double getRealMillisPerTick() {
        return getRealSecondsPerTick() * 1000.0;
    }
}
