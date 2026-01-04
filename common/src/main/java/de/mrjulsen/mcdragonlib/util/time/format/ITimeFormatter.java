package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

/**
 * Interface for formatting DLTime instances into strings.
 *
 * <p>Implementations must produce a textual representation for a given {@link DLTime}
 * and a {@link TimeContext}. For INGAME context a valid {@link ITimeSystem} should be supplied.
 */
public interface ITimeFormatter {
    /**
     * Format a DLTime for the given context and time system.
     *
     * @param time the DLTime to format
     * @param context REAL or INGAME interpretation
     * @param system ITimeSystem used when formatting INGAME values (may be null for REAL)
     * @return formatted string
     */
    String format(DLTime time, TimeContext context, ITimeSystem system);
}
