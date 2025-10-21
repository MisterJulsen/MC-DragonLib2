package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

public class TimeFormatDigitalDuration implements ITimeFormatter {

    private DLTime startTime;
    private boolean showMillis;
    private boolean showSeconds = true;
    private boolean showMinutes = true;
    private boolean showHours;
    private boolean showDays;

    public TimeFormatDigitalDuration() {}
    
    public TimeFormatDigitalDuration(DLTime startTicks) {
        this.startTime = startTicks;
    }

    public TimeFormatDigitalDuration(DLTime startTicks, boolean showMillis, boolean showSeconds, boolean showMinutes, boolean showHours, boolean showDays) {
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
            totalMillis = Math.abs(totalMillisOrTicks);
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
        }

        long millis = (long)(totalMillis % 1000);
        long totalSeconds = (long)(totalMillis / 1000);
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;
        long hours = totalHours % 24;
        long days = totalHours / 24;

        boolean showAnyMillis = showMillis;
        boolean showAnySeconds = showSeconds || showAnyMillis;
        boolean showAnyMinutes = showMinutes || showAnySeconds;
        boolean showAnyHours = showHours || showAnyMinutes;
        boolean showAnyDays = showDays || showAnyHours;

        boolean firstShownMillis = showMillis;
        boolean firstShownSeconds = !firstShownMillis && showSeconds;
        boolean firstShownMinutes = !firstShownMillis && !firstShownSeconds && showMinutes;

        StringBuilder sb = new StringBuilder();

        if ((showDays && showAnyDays) || (!showDays && days > 0)) {
            sb.append(days).append("d ");
        }

        if ((showHours && showAnyHours) || (!showHours && hours > 0)) {
            if (sb.length() > 0) sb.append(String.format("%02d", hours));
            else sb.append(hours);
        }

        if (showAnyMinutes) {
            if (firstShownMinutes || sb.length() > 0 || minutes > 0 || showMinutes)
                sb.append(sb.length() > 0 ? ":" : "").append(String.format("%02d", minutes));
        }

        if (showAnySeconds && (firstShownSeconds || sb.length() > 0 || seconds > 0 || showSeconds)) {
            sb.append(sb.length() > 0 ? ":" : "").append(String.format("%02d", seconds));
        }

        if (showMillis && (firstShownMillis || millis > 0)) {
            sb.append(",").append(String.format("%03d", millis));
        }

        if (sb.length() == 0) sb.append("0");

        String result = sb.toString();
        return negative ? "-" + result : result;
    }
}
