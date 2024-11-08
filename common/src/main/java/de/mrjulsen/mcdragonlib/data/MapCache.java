package de.mrjulsen.mcdragonlib.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import de.mrjulsen.mcdragonlib.config.ECachingPriority;

public class MapCache<T, S, I> {

    private transient final Map<Integer, T> cache = new ConcurrentHashMap<>();

    private transient final Function<I, T> delegate;
    private transient final Function<S, Integer> hashFunction;
    private transient final ECachingPriority priority;

    public MapCache(Function<I, T> delegate, Function<S, Integer> hashFunction, ECachingPriority priority) {
        this.delegate = delegate;
        this.hashFunction = hashFunction;
        this.priority = priority;
    }

    public MapCache(Function<I, T> delegate, Function<S, Integer> hashFunction) {
        this(delegate, hashFunction, ECachingPriority.NORMAL);
    }

    public T get(I input, S hashInput) {
        if (!priority.shouldCache()) {
            clearAll();
            return delegate.apply(input);
        }
        return cache.computeIfAbsent(hashFunction.apply(hashInput), x -> delegate.apply(input));
    }

    public void clearAll() {
        if (!cache.isEmpty()) cache.clear();
    }

    public void clear(S hashInput) {
        cache.remove(hashFunction.apply(hashInput));
    }
}
