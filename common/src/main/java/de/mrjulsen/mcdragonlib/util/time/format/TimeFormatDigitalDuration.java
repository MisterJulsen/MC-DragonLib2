package de.mrjulsen.mcdragonlib.util.time.format;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.DLTimeUnit;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

/**
 * Digital duration formatter producing colon-separated time (e.g. "01:23:45").
 *
 * <p>Configuration:
 * <ul>
 * <li>minUnit determines the smallest displayed field (SECONDS, MINUTES, ...).</li>
 * <li>includeMillisOrTicks toggles appending fractional millis (REAL) or ticks (INGAME).</li>
 * </ul>
 * 
 * <p>Two convenient default instances are provided for real and game contexts.
 */
public class TimeFormatDigitalDuration implements ITimeFormatter {

    public static final TimeFormatDigitalDuration DEFAULT_REAL_INSTANCE =
            new TimeFormatDigitalDuration(DLTimeUnit.SECONDS, false);

    public static final TimeFormatDigitalDuration DEFAULT_GAME_INSTANCE =
            new TimeFormatDigitalDuration(DLTimeUnit.MINUTES, false);

    private final DLTimeUnit minUnit;
    private final boolean includeMillisOrTicks;

    public TimeFormatDigitalDuration(DLTimeUnit minUnit, boolean includeMillisOrTicks) {
        if (minUnit == DLTimeUnit.TICKS || minUnit == DLTimeUnit.MILLIS) {
            this.minUnit = DLTimeUnit.SECONDS;
        } else {
            this.minUnit = minUnit;
        }
        this.includeMillisOrTicks = includeMillisOrTicks;
    }

    @Deprecated(forRemoval = true)
    public TimeFormatDigitalDuration() {
        this(DLTimeUnit.SECONDS, false);
    }
    
    @Deprecated(forRemoval = true)
    public TimeFormatDigitalDuration(DLTimeUnit startTicks) {
        this(DLTimeUnit.SECONDS, false);
    }

    @Deprecated(forRemoval = true)
    public TimeFormatDigitalDuration(DLTimeUnit startTicks, boolean showMillis, boolean showSeconds, boolean showMinutes, boolean showHours, boolean showDays) {
        this(DLTimeUnit.SECONDS, false);
    }

    /**
     * Format a DLTime into a digital duration string. For INGAME context an {@link ITimeSystem} is required.
     *
     * @param time time to format
     * @param context REAL or INGAME
     * @param system time system for INGAME context (nullable for REAL)
     * @return colon-separated duration string, optionally with fractional part
     * @throws IllegalArgumentException if context is INGAME and system is null
     */
    @Override
    public String format(DLTime time, TimeContext context, @Nullable ITimeSystem system) {
        long days = 0, hours = 0, minutes = 0, seconds = 0, millis = 0;

        if (context == TimeContext.REAL) {
            long remaining = (long) time.toRealMillis();

            if (minUnit.compareTo(DLTimeUnit.DAYS) <= 0) {
                days = (long)(remaining / DLTimeUnit.DAYS.millis);
                remaining -= days * DLTimeUnit.DAYS.millis;
            }
            if (minUnit.compareTo(DLTimeUnit.HOURS) <= 0) {
                hours = (long)(remaining / DLTimeUnit.HOURS.millis);
                remaining -= hours * DLTimeUnit.HOURS.millis;
            }
            if (minUnit.compareTo(DLTimeUnit.MINUTES) <= 0) {
                minutes = (long)(remaining / DLTimeUnit.MINUTES.millis);
                remaining -= minutes * DLTimeUnit.MINUTES.millis;
            }
            if (minUnit.compareTo(DLTimeUnit.SECONDS) <= 0) {
                seconds = (long)(remaining / DLTimeUnit.SECONDS.millis);
                remaining -= seconds * DLTimeUnit.SECONDS.millis;
            }
            millis = remaining;

        } else {
            if (system == null)
                throw new IllegalArgumentException("ITimeSystem required for INGAME formatting.");

            double ticks = time.toTicks(system);
            long ticksPerDay = system.getTicksPerDay();
            double ticksPerHour = ticksPerDay / 24.0;
            double ticksPerMinute = ticksPerHour / 60.0;
            double ticksPerSecond = ticksPerMinute / 60.0;

            if (minUnit.compareTo(DLTimeUnit.DAYS) <= 0) {
                days = (long) (ticks / ticksPerDay);
                ticks -= days * ticksPerDay;
            }
            if (minUnit.compareTo(DLTimeUnit.HOURS) <= 0) {
                hours = (long) (ticks / ticksPerHour);
                ticks -= hours * ticksPerHour;
            }
            if (minUnit.compareTo(DLTimeUnit.MINUTES) <= 0) {
                minutes = (long) (ticks / ticksPerMinute);
                ticks -= minutes * ticksPerMinute;
            }
            if (minUnit.compareTo(DLTimeUnit.SECONDS) <= 0) {
                seconds = (long) (ticks / ticksPerSecond);
                ticks -= seconds * ticksPerSecond;
            }
        }

        StringBuilder sb = new StringBuilder();
        boolean started = false;

        if (days > 0 || minUnit == DLTimeUnit.DAYS) {
            sb.append(String.format("%02d", days));
            started = true;
        }
        if (hours > 0 || started || minUnit == DLTimeUnit.HOURS) {
            if (started) sb.append(":");
            sb.append(String.format("%02d", hours));
            started = true;
        }
        if (minutes > 0 || started || minUnit == DLTimeUnit.MINUTES) {
            if (started) sb.append(":");
            sb.append(String.format("%02d", minutes));
            started = true;
        }
        if (seconds > 0 || started || minUnit == DLTimeUnit.SECONDS) {
            if (started) sb.append(":");
            sb.append(String.format("%02d", seconds));
        }

        if (includeMillisOrTicks) {
            if (context == TimeContext.REAL && millis > 0) {
                sb.append(".").append(String.format("%03d", millis));
            } else if (context == TimeContext.INGAME && system != null) {
                sb.append(".").append((long) time.toTicks(system)).append("t");
            }
        }

        return sb.toString();
    }
}
