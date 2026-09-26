package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class RegexSearchStrategy implements ISearchStrategy {

    public static final RegexSearchStrategy INSTANCE = new RegexSearchStrategy();

    private SearchQuery cachedQuery;
    private Pattern cachedPattern;

    @Override
    public boolean supports(SearchQuery query) {
        return !query.isEmpty() && query.regex();
    }

    @Override
    public SearchHit find(CharSequence text, SearchQuery query, int from, int to) {
        Pattern pattern = patternOf(query);
        if (pattern == null) {
            return null;
        }

        int end = Math.min(to, text.length());
        int start = Math.max(0, Math.min(from, end));

        Matcher matcher = pattern.matcher(text);
        matcher.useTransparentBounds(true);
        matcher.useAnchoringBounds(false);
        matcher.region(start, end);
        if (!matcher.find()) {
            return null;
        }

        String[] groups = new String[matcher.groupCount() + 1];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = matcher.group(i);
        }
        return new SearchHit(matcher.start(), matcher.end(), groups);
    }

    @Override
    public String expand(SearchHit hit, String replacement) {
        if (hit.groups() == null || replacement.indexOf('$') < 0) {
            return replacement;
        }

        StringBuilder result = new StringBuilder(replacement.length());
        for (int i = 0; i < replacement.length(); i++) {
            char c = replacement.charAt(i);
            if (c == '\\' && i + 1 < replacement.length()) {
                result.append(replacement.charAt(++i));
                continue;
            }
            if (c != '$' || i + 1 >= replacement.length() || !Character.isDigit(replacement.charAt(i + 1))) {
                result.append(c);
                continue;
            }
            int index = 0;
            int digits = 0;
            while (i + 1 < replacement.length() && Character.isDigit(replacement.charAt(i + 1)) && digits < 2) {
                index = index * 10 + (replacement.charAt(++i) - '0');
                digits++;
            }
            String group = hit.group(index);
            if (group != null) {
                result.append(group);
            }
        }
        return result.toString();
    }

    @Override
    public String name() {
        return "Regex";
    }

    public Pattern patternOf(SearchQuery query) {
        if (query.equals(cachedQuery)) {
            return cachedPattern;
        }
        cachedQuery = query;
        cachedPattern = compile(query);
        return cachedPattern;
    }

    private static Pattern compile(SearchQuery query) {
        String source = query.wholeWord() ? "\\b(?:" + query.pattern() + ")\\b" : query.pattern();
        int flags = query.caseSensitive() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        try {
            return Pattern.compile(source, flags);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }
}
