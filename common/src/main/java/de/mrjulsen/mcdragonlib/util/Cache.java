package de.mrjulsen.mcdragonlib.util;

import java.util.Optional;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.config.ECachingPriority;

/**
 * The cache creates data on the first request and stores it for faster reuse.
 * This is particularly useful for computationally intensive operations that
 * don't change frequently.
 */
public class Cache<T> {
    private T obj = null;
    private transient final Supplier<T> provider;
    private transient final ECachingPriority priority;

    /**
     * Create a new instance of the Cache.
     * @param provider The supplier that provides the data.
     * @param priority The priority with which data must be stored. To save RAM,
     * DragonLib has a configuration setting to define the priority data must have
     * to be stored in a cache. Data with a high performance impact
     * should have a higher priority than data with only a small performance impact,
     * where increased RAM consumption might be a problem. This parameter can be
     * omitted and is only useful in performance-critical situations.
     */
    public Cache(Supplier<T> provider, ECachingPriority priority) {
        this.provider = provider;
        this.priority = priority;
    }
    
    /**
     * Create a new instance of the Cache.
     * @param provider The supplier that provides the data.
     */
    public Cache(Supplier<T> provider) {
        this(provider, ECachingPriority.NORMAL);
    }

    /**
     * @return {@code true} if data is stored in the cache, {@code false} otherwise.
     */
    public boolean isCached() {
        return this.obj != null;
    }
    
    /**
     * @return the data stored in this cache and creates it on the first call.
     */
    public T get() {
        if (!priority.shouldCache()) {
            clear();
            return this.provider.get();
        }
        return !this.isCached() ? this.obj = this.provider.get() : this.obj;
    }

    /**
     * @return Returns the data only if it has already been created.
     */
    public Optional<T> getIfAvailable() {
        return !this.isCached() ? Optional.empty() : Optional.of(this.obj);
    }

    /**
     * Clears the currently stored data. The next time data is requested, it has to be created again first.
     */
    public void clear() {
        this.obj = null;
    }
}
