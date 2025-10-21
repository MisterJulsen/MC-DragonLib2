package de.mrjulsen.mcdragonlib.util.time;

import java.util.List;

public interface ITimeSystem {
    long getTicksPerDay();
    List<TimeZone> getTimeZones();

    default double getDaytimeOffset() {
        return getTicksPerDay() / 4;
    }

    /**
     * Calculates total real-life milliseconds for a given number of ticks,
     * starting at tick 0.
     */
    default double getRealMillisFromTicks(double totalTicks) {
        return getRealMillisFromTicks(totalTicks, 0.0);
    }

    /**
     * Calculates total real-life milliseconds for a given number of ticks,
     * starting from a specific tick offset (so time zones align correctly).
     */
    default double getRealMillisFromTicks(double totalTicks, double startTick) {
        if (totalTicks == 0)
            return 0;

        List<TimeZone> zones = getTimeZones().stream().sorted().toList();
        double remainingTicks = totalTicks;
        double realMillis = 0;
        long ticksPerDay = getTicksPerDay();
        double currentTick = startTick % ticksPerDay;

        while (remainingTicks > 0) {
            for (TimeZone zone : zones) {
                double zoneStart = zone.startTick();
                double zoneEnd = zone.endTick();
                double zoneMillisPerTick = zone.getRealMillisPerTick();

                double effectiveStart = Math.max(zoneStart, currentTick);

                if (effectiveStart < zoneEnd) {
                    double ticksInZone = zoneEnd - effectiveStart;
                    if (remainingTicks >= ticksInZone) {
                        realMillis += ticksInZone * zoneMillisPerTick;
                        remainingTicks -= ticksInZone;
                        currentTick = zoneEnd;
                    } else {
                        realMillis += remainingTicks * zoneMillisPerTick;
                        remainingTicks = 0;
                        break;
                    }
                }
            }
            currentTick = 0;
        }

        return realMillis;
    }

    /**
     * Calculates number of ticks that elapse in a given number of real-life
     * milliseconds, assuming the time starts at tick 0.
     */
    default double getTicksFromRealMillis(double totalMillis) {
        return getTicksFromRealMillis(totalMillis, 0.0);
    }

    /**
     * Calculates number of ticks that elapse in a given number of real-life
     * milliseconds, starting from a specific current tick.
     */
    default double getTicksFromRealMillis(double totalMillis, double startTick) {
        if (totalMillis == 0)
            return 0;

        double remainingMillis = totalMillis;
        double addedTicks = 0;
        long ticksPerDay = getTicksPerDay();

        List<TimeZone> zones = getTimeZones().stream().sorted().toList();
        double millisPerFullDay = getRealMillisFromTicks(ticksPerDay);
        double currentDayTick = startTick % ticksPerDay;
        double millisUntilEndOfDay = getRealMillisFromTicks(ticksPerDay - currentDayTick, currentDayTick);

        if (remainingMillis < millisUntilEndOfDay) {
            addedTicks = getTicksInPartialDay(remainingMillis, currentDayTick, zones);
            return addedTicks;
        }

        addedTicks += ticksPerDay - currentDayTick;
        remainingMillis -= millisUntilEndOfDay;
        long fullDays = (long) (remainingMillis / millisPerFullDay);
        addedTicks += fullDays * ticksPerDay;
        remainingMillis -= fullDays * millisPerFullDay;

        if (remainingMillis > 0) {
            addedTicks += getTicksInPartialDay(remainingMillis, 0, zones);
        }

        return addedTicks;
    }

    /**
     * Calculates number of ticks that elapse in a given number of real-life
     * milliseconds,
     * using a DLTime start reference.
     */
    default double getTicksFromRealMillis(double totalMillis, DLTime startTime) {
        return getTicksFromRealMillis(totalMillis, startTime.getTicks());
    }

    /**
     * Calculates real-life milliseconds that elapse for a given tick delta,
     * using a DLTime start reference.
     */
    default double getRealMillisFromTicks(double totalTicks, DLTime startTime) {
        return getRealMillisFromTicks(totalTicks, startTime.getTicks());
    }


    /*
     * INTERNAL HELPERS
     */

    private double getTicksInPartialDay(double millis, double startTick, List<TimeZone> zones) {
        double remainingMillis = millis;
        double addedTicks = 0;
        long ticksPerDay = getTicksPerDay();
        double currentTick = startTick % ticksPerDay;

        for (TimeZone zone : zones) {
            double zoneStart = zone.startTick();
            double zoneEnd = zone.endTick();
            double zoneMillisPerTick = zone.getRealMillisPerTick();

            double effectiveStart = Math.max(zoneStart, currentTick);

            if (effectiveStart < zoneEnd) {
                double ticksInZoneUntilEnd = zoneEnd - effectiveStart;
                double millisInZoneUntilEnd = ticksInZoneUntilEnd * zoneMillisPerTick;

                if (remainingMillis > 0) {
                    if (remainingMillis >= millisInZoneUntilEnd) {
                        addedTicks += ticksInZoneUntilEnd;
                        remainingMillis -= millisInZoneUntilEnd;
                        currentTick = zoneEnd;
                    } else {
                        addedTicks += remainingMillis / zoneMillisPerTick;
                        remainingMillis = 0;
                        break;
                    }
                }
            }
        }
        return addedTicks;
    }
}