package de.mrjulsen.mcdragonlib.util;

import java.util.Optional;
import java.util.function.Function;

import de.mrjulsen.mcdragonlib.config.ECachingPriority;

public class DataCache<T, D> {
    private T obj = null;
    private transient final Function<D, T> provider;
    private transient final ECachingPriority priority;

    public DataCache(Function<D, T> provider, ECachingPriority priority) {
        this.provider = provider;
        this.priority = priority;
    }
    public DataCache(Function<D, T> provider) {
        this(provider, ECachingPriority.NORMAL);
    }

    public boolean isCached() {
        return this.obj != null;
    }
    
    public T get(D data) {
        if (!priority.shouldCache()) {
            this.obj = null;
            return this.provider.apply(data);
        }
        return !this.isCached() ? this.obj = this.provider.apply(data) : this.obj;
    }

    public Optional<T> getIfAvailable() {
        return !this.isCached() ? Optional.empty() : Optional.of(this.obj);
    }

    public void clear() {
        this.obj = null;
    }
}