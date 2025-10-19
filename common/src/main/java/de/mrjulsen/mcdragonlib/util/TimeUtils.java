package de.mrjulsen.mcdragonlib.util;

import java.time.Duration;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.world.level.Level;

public final class TimeUtils {

    private static final byte TIME_SPLITTER_MINUTES_INDEX = 0;
    private static final byte TIME_SPLITTER_HOURS_INDEX = 1;
    private static final byte TIME_SPLITTER_DAYS_INDEX = 2;

    public static final long convertTicksToMs(long ticks) {
        return (long)(ticks * DragonLib.mspt());
    }

    public static final String formatDurationMs(long s) {
        Duration duration = Duration.ofMillis(s);
        long HH = duration.toHours();
        long MM = duration.toMinutesPart();
        long SS = duration.toSecondsPart();
        if (HH <= 0) {            
            return String.format("%02d:%02d", MM, SS);
        }        
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    public static long shiftDayTimeToMinecraftTicks(long time) {
        time = (time - DragonLib.daytimeShift()) % DragonLib.ticksPerDay();
        if (time < 0) {
            time += DragonLib.ticksPerDay();
        }
        return time;
    }

    private static long[] splitTime(long time) {
        long ticks = time % DragonLib.ticksPerDay();
        long days = time / DragonLib.ticksPerDay();
        long hours = ticks / DragonLib.ticksPerIngameHour();
        long minutes = ticks % DragonLib.ticksPerIngameHour();
        minutes = (long)((double)minutes / ((double)DragonLib.ticksPerIngameHour() / 60.0D));

        return new long[] {minutes, hours, days};
    }

    public static long dayTime(Level level) {
        return level.getDayTime() + DragonLib.daytimeShift();
    }

    public static double calcClockHandRotationDegrees(long time, double mod) {
        int modNumber = (int)(time % mod);
        double rotation = (modNumber / mod) * 360.0;
        return rotation;
    }

    public static long convertTicksToRealLife(long ticks) {
        return ticks / (DragonLib.ticksPerRealLifeDay() / DragonLib.ticksPerDay());
    }

    public static String parseTime(long time, TimeFormat format) {
        if (format == TimeFormat.TICKS) {
            return TimeUtils.shiftDayTimeToMinecraftTicks(time) + "t";
        }

        long[] splitTime = splitTime(time);
        long minutes = splitTime[TIME_SPLITTER_MINUTES_INDEX];
        long hours = splitTime[TIME_SPLITTER_HOURS_INDEX];
        
        if (format == TimeFormat.HOURS_24) {
            return String.format("%02d:%02d", hours, minutes);
        } else if (format == TimeFormat.HOURS_12) {
            String suffix = "AM";
            if (hours >= 12) {
                suffix = "PM";
                hours -= 12;
            }
            if (hours == 0) {
                hours = 12;
            }
        
            return String.format("%02d:%02d %s", hours, minutes, suffix);
        }

        return "";
    }

    public static String parseDurationScaled(long time) {
        return parseDuration(scaleTicks(time));
    }
    
    public static String parseDuration(long time) {
        if (time < 0) {
            return "-";
        }

        long[] splitTime = splitTime(time);
        long minutes = splitTime[TIME_SPLITTER_MINUTES_INDEX];
        long hours = splitTime[TIME_SPLITTER_HOURS_INDEX];
        long days = splitTime[TIME_SPLITTER_DAYS_INDEX];

        if (hours <= 0 && days <= 0) { 
            return TextUtils.translate(DragonLib.MODID + ".time_format.m", minutes).getString();
        } else if (days <= 0) { 
            return TextUtils.translate(DragonLib.MODID + ".time_format.hm", hours, minutes).getString();
        } else { 
            return TextUtils.translate(DragonLib.MODID + ".time_format.dhm", days, hours, minutes).getString();
        }
    }
        
    public static String parseDurationShortScaled(long time) {
        return parseDurationShort(scaleTicks(time));
    }

    public static String parseDurationShort(long time) {        
        if (time < 0) {
            return "-";
        }

        long[] splitTime = splitTime(time);
        long minutes = splitTime[TIME_SPLITTER_MINUTES_INDEX];
        long hours = splitTime[TIME_SPLITTER_HOURS_INDEX];
        long days = splitTime[TIME_SPLITTER_DAYS_INDEX];

        if (hours <= 0 && days <= 0) { 
            return String.format("%sm", minutes);
        } else if (days <= 0) { 
            return String.format("%sh %sm", hours, minutes);
        } else { 
            return String.format("%sd %sh %sm", days, hours, minutes);
        }
    }

    public static boolean isInRange(long time, long start, long end) {
        time = time % DragonLib.ticksPerDay();
        start = start % DragonLib.ticksPerDay();
        end = end % DragonLib.ticksPerDay();
        if (start <= end) {
            return time >= start && time <= end;
        } else {
            return time >= start || time <= end;
        }
    }

    /**
     * Adds a certain amount of ticks to the current tick value and possibly scales the value based on the time scale.
     * @param current The current tick value.
     * @param add The amount of ticks to add.
     * @param scale Whether the additional ticks should be scaled or not.
     * @return The new total tick time.
     */
    public static long addTime(long current, long add, boolean scale) {
        return scale ? current + scaleTicks(add) : current + add;
    }
    
    public static String formatTime(long time, TimeFormat format) {
        return TimeUtils.parseTime((time + DragonLib.daytimeShift()) % DragonLib.ticksPerDay(), format);
    }

    public static long formatToMinutes(long ticks) {
        return (long)((double)ticks / ((double)DragonLib.ticksPerIngameHour() / 60d));
    }

    /**
     * Scales the given ticks based on the time scale.
     * @param ticks The ticks to scale.
     * @return The scales tick value.
     */
    public static long scaleTicks(long ticks) {
        return (long)Math.ceil(ticks / ModCommonConfig.TIME_MULTIPLIER.get());
    }

    /**
     * Scales only the difference of the current ticks from a base value and returns the total time in ticks
     * @param total The total tick time.
     * @param base The base reference value.
     * @return The total time with scaled diff.
     */
    public static long scaleTicksDiff(long total, long base) {
        long diff = total - base;
        return base + scaleTicks(diff);
    }

    public static enum TimeFormat implements ITranslatableEnum {
        TICKS((byte)0, "ticks"),
        HOURS_24((byte)1, "hours_24"),
        HOURS_12((byte)2, "hours_12");
        
        private String format;
        private byte index;
        
        private TimeFormat(byte index, String format) {
            this.format = format;
            this.index = index;
        }
        
        public String getFormat() {
            return this.format;
        }

        public byte getIndex() {
            return this.index;
        }

        @Override
        public Data getTranslationData() {
            return new Data(DragonLib.MODID, "time_format", getFormat());
        }

        public static TimeFormat getFormatByIndex(byte index) {
            for (TimeFormat shape : TimeFormat.values()) {
                if (shape.getIndex() == index) {
                    return shape;
                }
            }
            return TimeFormat.TICKS;
        }

        @Override
        public String getSerializedName() {
            return this.format;
        }
    }    
}



