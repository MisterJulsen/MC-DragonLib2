package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

public enum CalloutKind {

    NOTE("NOTE", "Note"),
    TIP("TIP", "Tip"),
    IMPORTANT("IMPORTANT", "Important"),
    WARNING("WARNING", "Warning"),
    CAUTION("CAUTION", "Caution");

    private final String keyword;
    private final String title;

    CalloutKind(String keyword, String title) {
        this.keyword = keyword;
        this.title = title;
    }

    public String keyword() {
        return keyword;
    }

    public String title() {
        return title;
    }

    public static CalloutKind match(CharSequence line, int from, int to) {
        int start = from;
        int end = to;
        while (start < end && line.charAt(start) == ' ') {
            start++;
        }
        while (end > start && line.charAt(end - 1) == ' ') {
            end--;
        }
        if (end - start < 4 || line.charAt(start) != '[' || line.charAt(start + 1) != '!' || line.charAt(end - 1) != ']') {
            return null;
        }
        for (CalloutKind kind : values()) {
            if (matchesKeyword(line, start + 2, end - 1, kind.keyword)) {
                return kind;
            }
        }
        return null;
    }

    private static boolean matchesKeyword(CharSequence line, int from, int to, String keyword) {
        if (to - from != keyword.length()) {
            return false;
        }
        for (int i = 0; i < keyword.length(); i++) {
            if (Character.toUpperCase(line.charAt(from + i)) != keyword.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static CalloutKind byOrdinal(int ordinal) {
        CalloutKind[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NOTE;
    }
}
