package de.mrjulsen.mcdragonlib.client.gui.v2.text.search;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.document.TextRange;

public record SearchHit(int start, int end, String[] groups) {

    public static SearchHit of(int start, int end) {
        return new SearchHit(start, end, null);
    }

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return end <= start;
    }

    public TextRange range() {
        return new TextRange(start, end);
    }

    public String group(int index) {
        if (groups == null || index < 0 || index >= groups.length) {
            return null;
        }
        return groups[index];
    }
}
