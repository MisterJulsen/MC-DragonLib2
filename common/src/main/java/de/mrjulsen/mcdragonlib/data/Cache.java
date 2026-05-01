package de.mrjulsen.mcdragonlib.data;

import java.util.Optional;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.config.ECachingPriority;

public class Cache<T> {
    private T obj = null;
    private transient final Supplier<T> provider;
    private transient final ECachingPriority priority;

    public Cache(Supplier<T> provider, ECachingPriority priority) {
        this.provider = provider;
        this.priority = priority;
    }
    
    public Cache(Supplier<T> provider) {
        this(provider, ECachingPriority.NORMAL);
    }

    public boolean isCached() {
        return this.obj != null;
    }
    
    public T get() {
        if (!priority.shouldCache()) {
            clear();
            return this.provider.get();
        }
        return !this.isCached() ? this.obj = this.provider.get() : this.obj;
    }

    public Optional<T> getIfAvailable() {
        return !this.isCached() ? Optional.empty() : Optional.of(this.obj);
    }

    public void clear() {
        this.obj = null;
    }
}
