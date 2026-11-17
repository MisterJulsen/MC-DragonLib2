/*
 * DragonLib 3 - 1.20.1
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

package de.mrjulsen.mcdragonlib.client.model;

import java.util.Arrays;
import java.util.Objects;

public class ModelCacheKey {
    private final ModelContext context;
    private final Object[] additional;

    public ModelCacheKey(ModelContext context, Object... additional) {
        this.context = context;
        this.additional = additional;
    }

    public ModelContext context() {
        return context;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ModelCacheKey other)) return false;
        return Objects.equals(context, other.context) &&
               Arrays.deepEquals(additional, other.additional);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(context) + Arrays.deepHashCode(additional);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
          .append("[modelContext=").append(context)
          .append(", additional=").append(Arrays.deepToString(additional))
          .append("]");
        return sb.toString();
    }
}
