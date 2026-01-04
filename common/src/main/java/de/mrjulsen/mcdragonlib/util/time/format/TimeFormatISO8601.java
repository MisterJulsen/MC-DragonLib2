package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.DLTimeUnit;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

import org.jetbrains.annotations.Nullable;

/**
 * Formatter producing an ISO-8601 duration string in the form "P{d}DT{h}H{m}M{s}S".
 *
 * <p>For INGAME context the conversion uses the provided {@link ITimeSystem}.
 */
public class TimeFormatISO8601 implements ITimeFormatter {

    public static final TimeFormatISO8601 INSTANCE = new TimeFormatISO8601();

    @Override
    public String format(DLTime time, TimeContext context, @Nullable ITimeSystem system) {
        double totalSeconds = (context == TimeContext.REAL) ? time.toReal(DLTimeUnit.SECONDS) : time.toGameSeconds(system);
        long days = (long) (totalSeconds / 86400);
        long hours = (long) ((totalSeconds % 86400) / 3600);
        long minutes = (long) ((totalSeconds % 3600) / 60);
        long seconds = (long) (totalSeconds % 60);
        return String.format("P%dDT%dH%dM%dS", days, hours, minutes, seconds);
    }
}
