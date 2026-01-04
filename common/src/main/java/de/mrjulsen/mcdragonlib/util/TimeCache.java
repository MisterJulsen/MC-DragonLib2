package de.mrjulsen.mcdragonlib.util;

import java.util.Optional;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.config.ECachingPriority;

/**
 * The cache creates data on the first request and stores it for faster reuse.
 * This is particularly useful for computationally intensive operations that
 * don't change frequently.
 * <p>This type of cache stores data only for a specific period before it becomes
 * invalid. A TTL (Time to Live) value in milliseconds is defined to specify
 * how long the data remains valid. If the data is accessed after this time has
 * elapsed, it is recalculated, and the cache behaves as if no data were stored.</p>
 */
public class TimeCache<T> {
    private T obj = null;
    private transient final Supplier<T> provider;
    private transient final ECachingPriority priority;
    private transient final long ttl;
    private transient long refreshTime = Long.MIN_VALUE;

    /**
     * Create a new instance of the Cache.
     * @param provider The supplier that provides the data.
     * @param ttl The duration in milliseconds for which the data is valid and stored.
     * If the data is accessed after this time has elapsed, it is recalculated, and
     * the cache behaves as if no data were stored.
     * @param priority The priority with which data must be stored. To save RAM,
     * DragonLib has a configuration setting to define the priority data must have
     * to be stored in a cache. Data with a high performance impact
     * should have a higher priority than data with only a small performance impact,
     * where increased RAM consumption might be a problem. This parameter can be
     * omitted and is only useful in performance-critical situations.
     */
    public TimeCache(Supplier<T> provider, long ttl, ECachingPriority priority) {
        this.provider = provider;
        this.priority = priority;
        this.ttl = ttl;
    }
    
    /**
     * Create a new instance of the Cache.
     * @param provider The supplier that provides the data.
     * @param ttl The duration in milliseconds for which the data is valid and stored.
     * If the data is accessed after this time has elapsed, it is recalculated, and
     * the cache behaves as if no data were stored.
     */
    public TimeCache(Supplier<T> provider, long ttl) {
        this(provider, ttl, ECachingPriority.NORMAL);
    }

    /**
     * @return {@code true} if data is stored in the cache, {@code false} otherwise.
     */
    public boolean isCached() {
        return this.obj != null;
    }

    /**
     * @return The time in milliseconds at which the data was created. This value was set using {@link System#currentTimeMillis()}.
     */
    public long getLastRefreshedTimeMillis() {
        return refreshTime;
    }

    /**
     * @return The TTL (time to live) value indicates how long the data is valid.
     */
    public long getTTL() {
        return ttl;
    }

    /**
     * @return Checks if the data is still valid. Uses {@link System#currentTimeMillis()}.
     */
    public boolean isOutdated() {
        return System.currentTimeMillis() - getLastRefreshedTimeMillis() > ttl;
    }
    
    /**
     * @return the data stored in this cache and creates it on the first call.
     */
    public T get() {        
        if (isOutdated() || !priority.shouldCache()) {
            clear();
        }

        if (!priority.shouldCache()) {
            return this.provider.get();
        }

        if (isCached()) {
            return this.obj;
        } else {            
            refreshTime = System.currentTimeMillis();
            return this.obj = this.provider.get();
        }
    }

    /**
     * @return Returns the data only if it has already been created.
     */
    public Optional<T> getIfAvailable() {
        return (!this.isCached() || isOutdated()) ? Optional.empty() : Optional.of(this.obj);
    }

    /**
     * Clears the currently stored data. The next time data is requested, it has to be created again first.
     */
    public void clear() {
        refreshTime = Long.MIN_VALUE;
        this.obj = null;
    }
}
