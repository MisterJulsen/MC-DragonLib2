package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search;

public record SearchQuery(String pattern, boolean caseSensitive, boolean wholeWord, boolean regex) {

    public static final SearchQuery EMPTY = new SearchQuery("", false, false, false);

    public static SearchQuery of(String pattern) {
        return new SearchQuery(pattern == null ? "" : pattern, false, false, false);
    }

    public boolean isEmpty() {
        return pattern == null || pattern.isEmpty();
    }

    public SearchQuery withPattern(String pattern) {
        return new SearchQuery(pattern == null ? "" : pattern, caseSensitive, wholeWord, regex);
    }

    public SearchQuery withCaseSensitive(boolean caseSensitive) {
        return new SearchQuery(pattern, caseSensitive, wholeWord, regex);
    }

    public SearchQuery withWholeWord(boolean wholeWord) {
        return new SearchQuery(pattern, caseSensitive, wholeWord, regex);
    }

    public SearchQuery withRegex(boolean regex) {
        return new SearchQuery(pattern, caseSensitive, wholeWord, regex);
    }
}
