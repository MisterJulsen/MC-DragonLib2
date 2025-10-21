package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

public class TimeFormatTicks implements ITimeFormatter {

    public static final TimeFormatTicks INSTANCE = new TimeFormatTicks();

    public TimeFormatTicks() {}

    @Override
    public String format(DLTime time, TimeContext context) {
        return (long)time.getTicks() + "t";
    }
    
}
