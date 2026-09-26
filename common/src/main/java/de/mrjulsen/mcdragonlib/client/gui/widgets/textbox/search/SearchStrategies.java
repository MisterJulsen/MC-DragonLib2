package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SearchStrategies {

    private static final List<ISearchStrategy> STRATEGIES = new ArrayList<>(List.of(RegexSearchStrategy.INSTANCE, LiteralSearchStrategy.INSTANCE));

    private SearchStrategies() {
    }

    public static void register(ISearchStrategy strategy) {
        STRATEGIES.add(0, strategy);
    }

    public static boolean unregister(ISearchStrategy strategy) {
        return STRATEGIES.remove(strategy);
    }

    public static List<ISearchStrategy> all() {
        return Collections.unmodifiableList(STRATEGIES);
    }

    public static ISearchStrategy select(SearchQuery query) {
        for (ISearchStrategy strategy : STRATEGIES) {
            if (strategy.supports(query)) {
                return strategy;
            }
        }
        return null;
    }
}
