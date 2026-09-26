package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextDocument;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.IHighlightSource;

public class SearchSession implements IHighlightSource {

    public static final int DEFAULT_MATCH_LIMIT = 4096;

    private final TextDocument document;
    private final List<SearchHit> hits = new ArrayList<>();

    private SearchQuery query = SearchQuery.EMPTY;
    private ISearchStrategy strategy;
    private int activeIndex = -1;
    private boolean dirty = true;
    private int matchLimit = DEFAULT_MATCH_LIMIT;

    public SearchSession(TextDocument document) {
        this.document = document;
    }

    public TextDocument document() {
        return document;
    }

    public SearchQuery query() {
        return query;
    }

    public void setQuery(SearchQuery query) {
        this.query = query == null ? SearchQuery.EMPTY : query;
        this.activeIndex = -1;
        invalidate();
    }

    public int matchLimit() {
        return matchLimit;
    }

    public void setMatchLimit(int matchLimit) {
        this.matchLimit = Math.max(1, matchLimit);
        invalidate();
    }

    public ISearchStrategy strategy() {
        refresh();
        return strategy;
    }

    public boolean isActive() {
        return !query.isEmpty();
    }

    public void invalidate() {
        this.dirty = true;
    }

    public void clear() {
        setQuery(SearchQuery.EMPTY);
    }

    public List<SearchHit> hits() {
        refresh();
        return Collections.unmodifiableList(hits);
    }

    public int count() {
        refresh();
        return hits.size();
    }

    public boolean isTruncated() {
        refresh();
        return hits.size() >= matchLimit;
    }

    public int activeIndex() {
        refresh();
        return activeIndex;
    }

    public SearchHit activeHit() {
        refresh();
        return activeIndex >= 0 && activeIndex < hits.size() ? hits.get(activeIndex) : null;
    }

    public SearchHit select(int index) {
        refresh();
        if (hits.isEmpty()) {
            activeIndex = -1;
            return null;
        }
        int size = hits.size();
        activeIndex = ((index % size) + size) % size;
        return hits.get(activeIndex);
    }

    public SearchHit findNext(int fromOffset) {
        refresh();
        if (hits.isEmpty()) {
            activeIndex = -1;
            return null;
        }
        for (int i = 0; i < hits.size(); i++) {
            if (hits.get(i).start() >= fromOffset) {
                return select(i);
            }
        }
        return select(0);
    }

    public SearchHit findPrevious(int fromOffset) {
        refresh();
        if (hits.isEmpty()) {
            activeIndex = -1;
            return null;
        }
        for (int i = hits.size() - 1; i >= 0; i--) {
            if (hits.get(i).end() <= fromOffset) {
                return select(i);
            }
        }
        return select(hits.size() - 1);
    }

    public void shiftAfterEdit() {
        invalidate();
    }

    @Override
    public void forEachHighlight(int from, int to, IHighlightVisitor visitor) {
        refresh();
        if (hits.isEmpty()) {
            return;
        }
        int index = firstHitEndingAfter(from);
        for (int i = index; i < hits.size(); i++) {
            SearchHit hit = hits.get(i);
            if (hit.start() >= to) {
                break;
            }
            visitor.visit(hit.start(), hit.end(), i == activeIndex);
        }
    }

    private int firstHitEndingAfter(int offset) {
        int low = 0;
        int high = hits.size() - 1;
        int result = hits.size();
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (hits.get(mid).end() > offset) {
                result = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return result;
    }

    protected void refresh() {
        if (!dirty) {
            return;
        }
        dirty = false;
        hits.clear();
        strategy = SearchStrategies.select(query);
        if (strategy == null) {
            activeIndex = -1;
            return;
        }

        CharSequence text = document.characters();
        int length = text.length();
        int cursor = 0;
        while (cursor <= length && hits.size() < matchLimit) {
            SearchHit hit = strategy.find(text, query, cursor, length);
            if (hit == null) {
                break;
            }
            hits.add(hit);
            cursor = hit.end() > hit.start() ? hit.end() : hit.start() + 1;
        }
        if (activeIndex >= hits.size()) {
            activeIndex = hits.isEmpty() ? -1 : hits.size() - 1;
        }
    }
}
