package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

public class TimeFormaturVerboseDuration implements ITimeFormatter {

    private DLTime startTime;
    private boolean showMillis;
    private boolean showSeconds;
    private boolean showMinutes;
    private boolean showHours;
    private boolean showDays;    

    public TimeFormaturVerboseDuration() {}

    public TimeFormaturVerboseDuration(DLTime startTicks, boolean showMillis, boolean showSeconds, boolean showMinutes, boolean showHours, boolean showDays) {
        this.startTime = startTicks;
        this.showMillis = showMillis;
        this.showSeconds = showSeconds;
        this.showMinutes = showMinutes;
        this.showHours = showHours;
        this.showDays = showDays;
    }

    @Override
    public String format(DLTime time, TimeContext context) {
        boolean negative = false;
        double totalMillisOrTicks;
        ITimeSystem provider = time.getTimeSystem();

        double totalMillis = 0;
        if (context == TimeContext.REAL) {
            if (startTime != null) {
                double deltaTicks = time.getTicks() - startTime.getTicks();
                totalMillisOrTicks = provider.getRealMillisFromTicks(deltaTicks, startTime.getTicks());
            } else {
                totalMillisOrTicks = time.toRealMillis();
            }

            if (Double.isNaN(totalMillisOrTicks) || Double.isInfinite(totalMillisOrTicks)) {
                return "NaN";
            }
            if (totalMillisOrTicks < 0) negative = true;

            totalMillis = (long) Math.abs(Math.round(totalMillisOrTicks));

        } else {
            double deltaTicks;
            if (startTime != null) {
                deltaTicks = time.getTicks() - startTime.getTicks();
            } else {
                deltaTicks = time.getTicks();
            }

            if (Double.isNaN(deltaTicks) || Double.isInfinite(deltaTicks)) {
                return "NaN";
            }
            if (deltaTicks < 0) negative = true;

            totalMillis = Math.abs(deltaTicks / (provider.getTicksPerDay() / (24.0 * 60.0 * 60.0 * 1000.0)));
            /*
            long totalSeconds = (long) Math.round(totalGameSecondsDouble);

            long seconds = totalSeconds % 60;
            long totalMinutes = totalSeconds / 60;
            long minutes = totalMinutes % 60;
            long totalHours = totalMinutes / 60;
            long hours = totalHours % 24;
            long days = totalHours / 24;

            String result;
            StringBuilder sb = new StringBuilder();
            if (showDays && days > 0) sb.append(days).append("d ");
            if (showHours && hours > 0) sb.append(hours).append("h ");
            if (showMinutes && minutes > 0) sb.append(minutes).append("m ");
            if (showSeconds && seconds > 0) sb.append(seconds).append("s ");
            result = sb.toString().trim();
            if (result.isEmpty()) {
                if (showSeconds) result = "0s";
                else if (showMinutes) result = "0m";
                else if (showHours) result = "0h";
                else result = "0";
            }
            return negative ? "-" + result : result;
            */
        }

        
        long millis = (long)(totalMillis % 1000);
        long totalSeconds = (long)(totalMillis / 1000);
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;
        long hours = totalHours % 24;
        long days = totalHours / 24;

        String result;
        StringBuilder sb = new StringBuilder();
        if (showDays && days > 0) sb.append(days).append("d ");
        if (showHours && hours > 0) sb.append(hours).append("h ");
        if (showMinutes && minutes > 0) sb.append(minutes).append("m ");
        if (showSeconds && seconds > 0) sb.append(seconds).append("s ");
        if (showMillis && millis > 0) sb.append(millis).append("ms ");
        result = sb.toString().trim();
        if (result.isEmpty()) {
            if (showMillis) result = "0ms";
            else if (showSeconds) result = "0s";
            else if (showMillis) result = "0m";
            else result = "0";
        }
        return negative ? "-" + result : result;
    }
    
}
