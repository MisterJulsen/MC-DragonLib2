package de.mrjulsen.mcdragonlib.util;

import java.time.Duration;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

public final class TimeUtils {

    private static final byte TIME_SPLITTER_MINUTES_INDEX = 0;
    private static final byte TIME_SPLITTER_HOURS_INDEX = 1;
    private static final byte TIME_SPLITTER_DAYS_INDEX = 2;

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
        time = (time - DragonLib.DAYTIME_SHIFT) % DragonLib.TICKS_PER_DAY;
        if (time < 0) {
            time += DragonLib.TICKS_PER_DAY;
        }
        return time;
    }

    private static long[] splitTime(long time) {
        long ticks = time % DragonLib.TICKS_PER_DAY;
        long days = ticks / DragonLib.TICKS_PER_DAY;
        long hours = ticks / DragonLib.TICKS_PER_INGAME_HOUR;
        long minutes = ticks % DragonLib.TICKS_PER_INGAME_HOUR;
        minutes = (long)(minutes / ((double)DragonLib.TICKS_PER_INGAME_HOUR / 60.0D));

        return new long[] {minutes, hours, days};
    }

    public static long dayTime(Level level) {
        return level.getDayTime() + DragonLib.DAYTIME_SHIFT;
    }

    public static double calcClockHandRotationDegrees(long time, double mod) {
        int modNumber = (int)(time % mod);
        double rotation = (modNumber / mod) * 360.0;
        return rotation;
    }

    public static long convertTicksToRealLife(long ticks) {
        return ticks / (DragonLib.TICKS_PER_REAL_LIFE_DAY / DragonLib.TICKS_PER_DAY);
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
    
    public static String parseDurationShort(int time) {        
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
        time = time % DragonLib.TICKS_PER_DAY;
        start = start % DragonLib.TICKS_PER_DAY;
        end = end % DragonLib.TICKS_PER_DAY;
        if (start <= end) {
            return time >= start && time <= end;
        } else {
            return time >= start || time <= end;
        }
    }

    public static enum TimeFormat implements StringRepresentable, ITranslatableEnum {
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

        public String getTranslationKey() {
            return String.format("%s.time_format.%s", DragonLib.MODID, format);
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

        @Override
        public String getEnumName() {
            return "time_format";
        }

        @Override
        public String getEnumValueName() {
            return getFormat();
        }
    }    
}



