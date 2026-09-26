package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search;

public interface ISearchStrategy {

    boolean supports(SearchQuery query);

    SearchHit find(CharSequence text, SearchQuery query, int from, int to);

    default String expand(SearchHit hit, String replacement) {
        return replacement;
    }

    String name();
}
