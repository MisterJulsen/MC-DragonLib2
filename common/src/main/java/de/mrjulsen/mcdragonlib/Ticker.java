package de.mrjulsen.mcdragonlib;

public class Ticker {

    private long ticks;

    void tick() {
        ticks++;
    }

    void reset() {
        ticks = 0;
    }

    public long getTicks() {
        return ticks;
    }
}
