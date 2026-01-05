package de.mrjulsen.mcdragonlib.util.time.format;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.DLTimeUnit;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

/**
 * Formatter that produces a human-readable, verbose duration string.
 *
 * <p>Depending on {@link TimeContext} it formats either real-world durations (days/hours/minutes/seconds/millis)
 * or in-game durations (days/hours/minutes/seconds/ticks) using the provided {@link ITimeSystem}.
 *
 * <p>The behaviour is controlled by {@link Config} which allows toggling which components are shown.
 */
public class TimeFormatVerboseDuration implements ITimeFormatter {

    public static final TimeFormatVerboseDuration DEFAULT_INSTANCE = new TimeFormatVerboseDuration(new Config().showDays(true).showHours(true).showMinutes(true).showSeconds(true));

    private final Config config;

    /**
     * Create a formatter with the given configuration.
     *
     * @param config formatting configuration (non-null)
     */
    public TimeFormatVerboseDuration(Config config) {
        this.config = config;
    }

    /**
     * Format the given {@link DLTime} to a verbose human-readable duration string.
     *
     * <p>For TimeContext.REAL the method decomposes real milliseconds into configured fields.
     * For TimeContext.INGAME it decomposes ticks according to {@code system} and may append ticks.
     *
     * @param time the time to format
     * @param context whether formatting is REAL or INGAME
     * @param system the ITimeSystem required for INGAME formatting (nullable for REAL)
     * @return formatted duration string (returns "0" when nothing to show)
     * @throws IllegalArgumentException if context is INGAME and system is null
     */
    @Override
    public String format(DLTime time, TimeContext context, @Nullable ITimeSystem system) {
        StringBuilder sb = new StringBuilder();

        if (context == TimeContext.REAL) {
            long remaining = (long) time.toRealMillis();

            if (config.showDays) {
                long d = (long)(remaining / DLTimeUnit.DAYS.millis);
                if (d > 0) { sb.append(d).append("d "); remaining -= d * DLTimeUnit.DAYS.millis; }
            }
            if (config.showHours) {
                long h = (long)(remaining / DLTimeUnit.HOURS.millis);
                if (h > 0) { sb.append(h).append("h "); remaining -= h * DLTimeUnit.HOURS.millis; }
            }
            if (config.showMinutes) {
                long m = (long)(remaining / DLTimeUnit.MINUTES.millis);
                if (m > 0) { sb.append(m).append("m "); remaining -= m * DLTimeUnit.MINUTES.millis; }
            }
            if (config.showSeconds) {
                long s = (long)(remaining / DLTimeUnit.SECONDS.millis);
                if (s > 0) { sb.append(s).append("s "); remaining -= s * DLTimeUnit.SECONDS.millis; }
            }
            if (config.showMillis) {
                long ms = remaining;
                if (ms > 0) sb.append(ms).append("ms ");
            }

        } else {
            if (system == null)
                throw new IllegalArgumentException("ITimeSystem required for INGAME formatting.");

            double ticks = time.toTicks(system);
            long ticksPerDay = system.getTicksPerDay();
            double ticksPerHour = ticksPerDay / 24.0;
            double ticksPerMinute = ticksPerHour / 60.0;
            double ticksPerSecond = ticksPerMinute / 60.0;

            if (config.showDays) {
                long d = (long) (ticks / ticksPerDay);
                if (d > 0) { sb.append(d).append("d "); ticks -= d * ticksPerDay; }
            }
            if (config.showHours) {
                long h = (long) (ticks / ticksPerHour);
                if (h > 0) { sb.append(h).append("h "); ticks -= h * ticksPerHour; }
            }
            if (config.showMinutes) {
                long m = (long) (ticks / ticksPerMinute);
                if (m > 0) { sb.append(m).append("m "); ticks -= m * ticksPerMinute; }
            }
            if (config.showSeconds) {
                long s = (long) (ticks / ticksPerSecond);
                if (s > 0) sb.append(s).append("s ");
            }
            if (config.showTicks) {
                sb.append((long) time.toTicks(system)).append("t ");
            }
        }

        String result = sb.toString().trim();
        return result.isEmpty() ? "0" : result;
    }

    /**
     * Configuration object controlling which fields are emitted by the formatter.
     */
    public static class Config {
        private boolean showDays = false;
        private boolean showHours = false;
        private boolean showMinutes = false;
        private boolean showSeconds = false;
        private boolean showMillis = false;
        private boolean showTicks = false;

        public Config showDays(boolean b) {
            this.showDays = b;
            return this;
        }
        public Config showHours(boolean b) {
            this.showHours = b;
            return this;
        }
        public Config showMinutes(boolean b) {
            this.showMinutes = b;
            return this;
        }
        public Config showSeconds(boolean b) {
            this.showSeconds = b;
            return this;
        }
        public Config showMillis(boolean b) {
            this.showMillis = b;
            return this;
        }
        public Config showTicks(boolean b) {
            this.showTicks = b;
            return this;
        }
    }
}

