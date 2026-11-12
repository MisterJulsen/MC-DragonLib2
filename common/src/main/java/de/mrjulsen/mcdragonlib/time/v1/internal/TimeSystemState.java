package de.mrjulsen.mcdragonlib.time.v1.internal;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.time.v1.TimeSystem;
import de.mrjulsen.mcdragonlib.time.v1.TimeSystems;

/**
 * The time systems that come from outside the code: the datapack one and the one a server sent.
 */
public final class TimeSystemState {

    private static volatile TimeSystem datapack;
    private static volatile TimeSystem synced;

    private TimeSystemState() {}

    @Nullable
    public static TimeSystem datapack() {
        return datapack;
    }

    @Nullable
    public static TimeSystem synced() {
        return synced;
    }

    static void setDatapack(@Nullable TimeSystem system) {
        datapack = system;
        TimeSystems.invalidate();
    }

    static void setSynced(@Nullable TimeSystem system) {
        synced = system;
    }
}
