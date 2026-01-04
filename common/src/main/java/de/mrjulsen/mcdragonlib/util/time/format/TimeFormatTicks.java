package de.mrjulsen.mcdragonlib.util.time.format;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

/**
 * Simple formatter that renders a duration as ticks (e.g. "123t").
 *
 * <p>When formatting in INGAME context an {@link ITimeSystem} is required to resolve ticks.
 * In REAL context the method converts real milliseconds to ticks using a fixed 50ms/tick.
 */
public class TimeFormatTicks implements ITimeFormatter {
    
    public static final TimeFormatTicks INSTANCE = new TimeFormatTicks();

    @Override
    public String format(DLTime time, TimeContext context, @Nullable ITimeSystem system) {
        if (system == null && context == TimeContext.INGAME) {
            throw new IllegalArgumentException("ITimeSystem required for ticks");
        }
        long ticks = (context == TimeContext.REAL) ? (long) (time.toRealMillis() / 50.0) : (long) time.toTicks(system);
        return ticks + "t";
    }
}
