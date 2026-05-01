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
