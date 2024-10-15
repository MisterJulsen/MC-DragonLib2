package de.mrjulsen.mcdragonlib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class ListUtils {
    
    /**
     * Compares two lists and checks whether all elements are equal and in the same order.
     * @param <T> Any object
     * @param a First list
     * @param b Second list
     * @param comparator Custom compare function
     * @return {@code true} if the two lists contain the same objects in the same order. {@code false} otherwise.
     */
    public static <T> boolean compareCollections(Collection<T> a, Collection<T> b, BiPredicate<T, T> comparator) {
        if (a.size() != b.size()) {
            return false;
        }

        Iterator<T> i = a.iterator();
        Iterator<T> k = b.iterator();

        while (i.hasNext() && k.hasNext()) {
            if (!comparator.test(i.next(), k.next())) {
                return false;
            }
        }

        return true;
    }

    public static <Key, Value> boolean areEqual(Set<Map.Entry<Key, Value>> set1, Set<Map.Entry<Key, Value>> set2) {
        if (set1.size() != set2.size()) {
            return false;
        }

        for (Map.Entry<Key, Value> entry : set1) {
            if(!set2.contains(entry)) {
                return false;
            }
        }

        return true;
    }
    
    public static <T> void iterateLooped(List<T> list, int startIndex, BiConsumer<Integer, T> action) {
        for (int i = 0; i < list.size(); i++) {
            int j = (i + startIndex) % list.size();
            action.accept(j, list.get(j));
        }
    }

    public static <T> List<T> getNextN(List<T> list, int startIndex, int count) {
        if (count > list.size()) {
            throw new IndexOutOfBoundsException("The number of elements to be obtained is greater than the list.");
        }
        List<T> elements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int j = (i + startIndex) % list.size();
            elements.add(list.get(j));
        }
        return elements;
    }
    
    public static <T> Optional<T> getNext(List<T> list, int startIndex, BiPredicate<Integer, T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            int j = (i + startIndex) % list.size();
            if (predicate.test(j, list.get(j))) {
                return Optional.of(list.get(j));
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getPrevious(List<T> list, int startIndex, BiPredicate<Integer, T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            int j = (i + startIndex) % list.size();
            if (predicate.test(j, list.get(j))) {
                return Optional.of(list.get(j));
            }
        }
        return Optional.empty();
    }
}
