/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.util;

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

    public int getCachedDataCount() {
        return cache.size();
    }
}
