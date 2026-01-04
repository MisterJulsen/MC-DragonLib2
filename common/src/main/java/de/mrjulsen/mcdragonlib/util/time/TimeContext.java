package de.mrjulsen.mcdragonlib.util.time;

/**
 * Context hint used when formatting or extracting time values.
 *
 * <p>INGAME: interpret values using an {@link ITimeSystem} (game ticks/seconds/minutes/hours/days).
 * REAL: interpret values as real-world durations (milliseconds/seconds/minutes/etc).
 */
public enum TimeContext {
    /**
     * Interpret times using an ITimeSystem (game-relative).
     */
    INGAME,
    /**
     * Interpret times as real-world values (milliseconds/seconds/etc).
     */
    REAL
}
