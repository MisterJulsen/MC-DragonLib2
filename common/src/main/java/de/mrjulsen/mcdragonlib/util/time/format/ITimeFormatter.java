package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

public interface ITimeFormatter {
    String format(DLTime time, TimeContext context);
}
