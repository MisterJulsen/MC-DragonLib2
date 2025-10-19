package de.mrjulsen.mcdragonlib.client.newgui.widgets.util;

import java.util.Locale;

public interface INumberFormatAdapter {
    public abstract String format(double value);
    public abstract double parse(String input) throws NumberFormatException;

    public static class DecimalNumberFormat implements INumberFormatAdapter {

        protected final int decimals;

        public DecimalNumberFormat(int decimals) {
            this.decimals = decimals;
        }

        @Override
        public String format(double value) {
            return String.format(Locale.US, "%." + decimals + "f", value);
        }

        @Override
        public double parse(String input) throws NumberFormatException {
            return decimals <= 0
                ? Integer.parseInt(input)
                : Double.parseDouble(input);
        }
    }



    public static class HexNumberFormat implements INumberFormatAdapter {

        protected final boolean withHash;

        public HexNumberFormat(boolean withHash) {
            this.withHash = withHash;
        }
        
        public HexNumberFormat() {
            this(false);
        }

        @Override
        public String format(double value) {
            long intValue = (long) value;
            String hex = Long.toHexString(intValue).toUpperCase(Locale.ROOT);
            return withHash ? "#" + hex : hex;
        }

        @Override
        public double parse(String input) throws NumberFormatException {
            String clean = input.trim();
            if (clean.startsWith("#")) {
                clean = clean.substring(1);
            }
            return Long.parseLong(clean, 16);
        }
    }

    

    public static class HexARGBColorFormat implements INumberFormatAdapter {

        protected final boolean includeAlpha;

        public HexARGBColorFormat(boolean includeAlpha) {
            this.includeAlpha = includeAlpha;
        }
        
        public HexARGBColorFormat() {
            this(false);
        }

        @Override
        public String format(double value) {
            int argb = (int) value;
            if (includeAlpha) {
                return String.format(Locale.ROOT, "#%08X", argb);
            } else {
                int rgb = argb & 0x00FFFFFF;
                return String.format(Locale.ROOT, "#%06X", rgb);
            }
        }

        @Override
        public double parse(String input) throws NumberFormatException {
            String clean = input.trim();
            if (clean.startsWith("#")) {
                clean = clean.substring(1);
            }
            if (clean.startsWith("0x") || clean.startsWith("0X")) {
                clean = clean.substring(2);
            }

            if (clean.length() == 6) {
                int rgb = Integer.parseUnsignedInt(clean, 16);
                int argb = 0xFF000000 | rgb;
                return (double) argb;
            } else if (clean.length() == 8) {
                int argb = Integer.parseUnsignedInt(clean, 16);
                return (double) argb;
            } else {
                throw new NumberFormatException("Invalid hex color format: " + input);
            }
        }
    }


    

    public static class UnitNumberFormat extends DecimalNumberFormat {

        protected final String unit;

        public UnitNumberFormat(int decimals, String unit) {
            super(decimals);
            this.unit = unit;
        }

        @Override
        public String format(double value) {
            return String.format(Locale.US, "%." + decimals + "f%s", value, unit);
        }

        @Override
        public double parse(String input) throws NumberFormatException {
            String clean = input.replace(unit, "").trim();
            return Double.parseDouble(clean);
        }
    }

    

    public static class TimeNumberFormat implements INumberFormatAdapter {
        @Override
        public String format(double value) {
            int totalSeconds = (int) value;
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        @Override
        public double parse(String input) throws NumberFormatException {
            String[] parts = input.split(":");
            if (parts.length != 3) throw new NumberFormatException("Invalid time format");
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());
            int seconds = Integer.parseInt(parts[2].trim());
            return hours * 3600 + minutes * 60 + seconds;
        }
    }
}
