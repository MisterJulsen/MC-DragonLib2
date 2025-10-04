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
    

    public static class UnitNumberFormat extends DecimalNumberFormat {

        protected final String unit;

        public UnitNumberFormat(int decimals, String unit) {
            super(decimals);
            this.unit = unit;
        }

        @Override
        public String format(double value) {
            return String.format(Locale.US, "%." + decimals + "f " + unit, value);
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
