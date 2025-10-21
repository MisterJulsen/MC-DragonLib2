package de.mrjulsen.mcdragonlib.util.time;

import net.minecraft.world.level.Level;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.mrjulsen.mcdragonlib.util.time.format.ITimeFormatter;

/**
 * Immutable, provider-aware time abstraction for Minecraft and real-life time.
 * Fully compatible with DLTime Mods via ITimeSystem.
 */
public final class DLTime implements Comparable<DLTime> {
    private final double ticks;
    private final ITimeSystem provider;

    private DLTime(double ticks, ITimeSystem provider) {
        this.ticks = ticks;
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    /* ======================================================
     * FACTORY METHODS
     * ====================================================== */

    public static DLTime fromTicks(double ticks, ITimeSystem provider) {
        return new DLTime(ticks, provider);
    }

    public static DLTime fromIngame(long days, int hours, int minutes, int ticks, ITimeSystem provider) {
        long totalTicks = days * provider.getTicksPerDay()
                + (long) hours * provider.getTicksPerDay() / 24
                + (long) minutes * provider.getTicksPerDay() / (24 * 60)
                + ticks;
        return new DLTime(totalTicks, provider);
    }

    public static DLTime fromReal(long days, int hours, int minutes, int seconds, int millis, ITimeSystem provider) {
        long totalMillis = millis
                + TimeUnit.SECONDS.toMillis(seconds)
                + TimeUnit.MINUTES.toMillis(minutes)
                + TimeUnit.HOURS.toMillis(hours)
                + TimeUnit.DAYS.toMillis(days);
        double ticks = provider.getTicksFromRealMillis(totalMillis);
        
        return new DLTime(ticks, provider);
    }

    public static DLTime fromLevelTime(Level level, ITimeSystem provider) {
        return new DLTime(level.getDayTime(), provider);
    }
    public static DLTime now(ITimeSystem provider) {
        long millis = System.currentTimeMillis();
        double ticks = provider.getTicksFromRealMillis(millis);
        return new DLTime(ticks, provider);
    }

    /* ======================================================
     * CONVERSION
     * ====================================================== */

    public double getTicks() {
        return ticks;
    }

    public double toRealMillis() {
        return provider.getRealMillisFromTicks(ticks);
    }

    public double toRealSeconds() {
        return toRealMillis() / 1000.0;
    }

    public double toRealMinutes() {
        return toRealSeconds() / 60.0;
    }

    public double toRealHours() {
        return toRealMinutes() / 60.0;
    }

    public double toRealDays() {
        return toRealHours() / 24.0;
    }

    public double toGameDays() {
        return ticks / provider.getTicksPerDay();
    }

    public double toGameHours() {
        return toGameDays() * 24.0;
    }

    public double toGameMinutes() {
        return toGameHours() * 60.0;
    }

    public double toGameSeconds() {
        return toGameMinutes() * 60.0;
    }

    /* ======================================================
     * OPERATIONS
     * ====================================================== */

    public DLTime addTicks(double ticks) {
        return new DLTime(this.ticks + ticks, provider);
    }

    public DLTime addGameDays(long days) {
        return addTicks(days * provider.getTicksPerDay());
    }

    public DLTime addGameHours(long hours) {
        double ticksPerHour = (double)provider.getTicksPerDay() / 24;
        return addTicks(hours * ticksPerHour);
    }

    public DLTime addGameMinutes(long minutes) {
        double ticksPerMinute = (double)provider.getTicksPerDay() / (24 * 60);
        return addTicks(minutes * ticksPerMinute);
    }

    public DLTime addGameSeconds(long seconds) {
        double ticksPerSecond = (double)provider.getTicksPerDay() / (24 * 60 * 60);
        return addTicks(seconds * ticksPerSecond);
    }


    
    public DLTime addRealMilliseconds(long ms) {
        double ticksToAdd = provider.getTicksFromRealMillis(ms, this.ticks);
        return addTicks(ticksToAdd);
    }

    public DLTime addRealSeconds(long seconds) {
        return addRealMilliseconds(seconds * 1000);
    }

    public DLTime addRealMinutes(long minutes) {
        return addRealSeconds(minutes * 60);
    }

    public DLTime addRealHours(long hours) {
        return addRealMinutes(hours * 60);
    }

    public DLTime addRealDays(long days) {
        return addRealHours(days * 24);
    }


    public DLTime addTime(DLTime other) {
        return addTicks(other.ticks, other.provider);
    }
    
    public DLTime subTime(DLTime other) {
        return addTicks(-other.ticks, other.provider);
    }
    
    
    public DLTime addTicks(double ticksToAdd, ITimeSystem fromProvider) {
        double realMillis = fromProvider.getRealMillisFromTicks(ticksToAdd);
        return this.addRealMilliseconds((long)Math.round(realMillis));
    }

    public DLTime addGameDays(long days, ITimeSystem fromProvider) {
        double ticksToAdd = fromProvider.getTicksPerDay() * days;
        return addTicks(ticksToAdd, fromProvider);
    }    

    public DLTime addGameHours(long hours, ITimeSystem fromProvider) {
        double ticksPerHour = (double)fromProvider.getTicksPerDay() / 24.0;
        double ticksToAdd = ticksPerHour * hours;
        return addTicks(ticksToAdd, fromProvider);
    }
    
    public DLTime addGameMinutes(long minutes, ITimeSystem fromProvider) {
        double ticksPerMinute = (double)fromProvider.getTicksPerDay() / (24.0 * 60.0);
        double ticksToAdd = ticksPerMinute * minutes;
        return addTicks(ticksToAdd, fromProvider);
    }
    
    public DLTime addGameSeconds(long seconds, ITimeSystem fromProvider) {
        double ticksPerSecond = (double)fromProvider.getTicksPerDay() / (24.0 * 60.0 * 60.0);
        double ticksToAdd = ticksPerSecond * seconds;
        return addTicks(ticksToAdd, fromProvider);
    }



    
    public DLTime addRealMilliseconds(long ms, ITimeSystem fromProvider) {
        double sourceTicks = fromProvider.getTicksFromRealMillis(ms);
        double converted = convertTicks(sourceTicks, fromProvider, this.provider);
        return addTicks(converted);
    }

    public DLTime addRealSeconds(long seconds, ITimeSystem fromProvider) {
        return addRealMilliseconds(seconds * 1000, fromProvider);
    }
    
    public DLTime addRealMinutes(long minutes, ITimeSystem fromProvider) {
        return addRealSeconds(minutes * 60, fromProvider);
    }
    
    public DLTime addRealHours(long hours, ITimeSystem fromProvider) {
        return addRealMinutes(hours * 60, fromProvider);
    }
    
    public DLTime addRealDays(long days, ITimeSystem fromProvider) {
        return addRealHours(days * 24, fromProvider);
    }

    
    private static double convertTicks(double sourceTicks, ITimeSystem from, ITimeSystem to) {
        double realMillis = from.getRealMillisFromTicks(sourceTicks);
        return to.getTicksFromRealMillis(realMillis);
    }


    @Override
    public int compareTo(DLTime o) {
        return Double.compare(this.toRealMillis(), o.toRealMillis());
    }

    public boolean isBefore(DLTime other) {
        return this.toRealMillis() < other.toRealMillis();
    }

    public boolean isAfter(DLTime other) {
        return this.toRealMillis() > other.toRealMillis();
    }

    public boolean isBetween(DLTime start, DLTime end) {
        double thisMillis = this.toRealMillis();
        double startMillis = start.toRealMillis();
        double endMillis = end.toRealMillis();
        
        if (startMillis > endMillis) {
            return thisMillis >= endMillis && thisMillis <= startMillis;
        }
        return thisMillis >= startMillis && thisMillis <= endMillis;
    }


    /* ======================================================
     * COMPARABLE / MISC
     * ====================================================== */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DLTime other)) return false;
        return ticks == other.ticks && provider.equals(other.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticks, provider);
    }

    @Override
    public String toString() {
        return "DLTime[" + ticks + " ticks, provider=" + provider + "]";
    }

    public ITimeSystem getTimeSystem() {
        return provider;
    }


    public String format(ITimeFormatter formatter, TimeContext context) {
        return formatter.format(this, context);
    }

}