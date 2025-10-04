package de.mrjulsen.mcdragonlib.client.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;

public final class ModelContext {
    public static final ModelContext EMPTY = ModelContext.builder().build();

    private final Map<ModelProperty<?>, Object> properties;

    private ModelContext(Map<ModelProperty<?>, Object> properties) {
        this.properties = properties;
    }

    public Set<ModelProperty<?>> getProperties()
    {
        return properties.keySet();
    }

    public boolean has(ModelProperty<?> property) {
        return properties.containsKey(property);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ModelProperty<T> property) {
        return (T)properties.get(property);
    }

    public Builder derive() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder(null);
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        for (Map.Entry<ModelProperty<?>, Object> entry : properties.entrySet()) {
            int keyHash = Objects.hashCode(entry.getKey());
            int valueHash = deepValueHashCode(entry.getValue());
            result = 31 * result + (keyHash ^ valueHash);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ModelContext other)) return false;
        if (properties.size() != other.properties.size()) return false;

        for (Map.Entry<ModelProperty<?>, Object> entry : properties.entrySet()) {
            ModelProperty<?> key = entry.getKey();
            Object value1 = entry.getValue();
            Object value2 = other.properties.get(key);
            if (!deepEquals(value1, value2)) return false;
        }

        return true;
    }

    private static boolean deepEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getClass().isArray() && b.getClass().isArray()) {
            if (a instanceof Object[] && b instanceof Object[]) {
                return Arrays.deepEquals((Object[]) a, (Object[]) b);
            } else if (a instanceof byte[] && b instanceof byte[]) {
                return Arrays.equals((byte[]) a, (byte[]) b);
            } else if (a instanceof short[] && b instanceof short[]) {
                return Arrays.equals((short[]) a, (short[]) b);
            } else if (a instanceof int[] && b instanceof int[]) {
                return Arrays.equals((int[]) a, (int[]) b);
            } else if (a instanceof long[] && b instanceof long[]) {
                return Arrays.equals((long[]) a, (long[]) b);
            } else if (a instanceof float[] && b instanceof float[]) {
                return Arrays.equals((float[]) a, (float[]) b);
            } else if (a instanceof double[] && b instanceof double[]) {
                return Arrays.equals((double[]) a, (double[]) b);
            } else if (a instanceof char[] && b instanceof char[]) {
                return Arrays.equals((char[]) a, (char[]) b);
            } else if (a instanceof boolean[] && b instanceof boolean[]) {
                return Arrays.equals((boolean[]) a, (boolean[]) b);
            }
        }
        return Objects.equals(a, b);
    }

    private static int deepValueHashCode(Object value) {
        if (value == null) return 0;
        if (value.getClass().isArray()) {
            if (value instanceof Object[]) return Arrays.deepHashCode((Object[]) value);
            else if (value instanceof byte[]) return Arrays.hashCode((byte[]) value);
            else if (value instanceof short[]) return Arrays.hashCode((short[]) value);
            else if (value instanceof int[]) return Arrays.hashCode((int[]) value);
            else if (value instanceof long[]) return Arrays.hashCode((long[]) value);
            else if (value instanceof float[]) return Arrays.hashCode((float[]) value);
            else if (value instanceof double[]) return Arrays.hashCode((double[]) value);
            else if (value instanceof char[]) return Arrays.hashCode((char[]) value);
            else if (value instanceof boolean[]) return Arrays.hashCode((boolean[]) value);
        }
        return value.hashCode();
    }

    public static final class Builder {
        private final Map<ModelProperty<?>, Object> properties = new IdentityHashMap<>();

        private Builder(ModelContext parent) {
            if (parent != null) {
                properties.putAll(parent.properties);
            }
        }
        
        public <T> Builder with(ModelProperty<T> property, T value) {
            Preconditions.checkState(property.test(value), "The provided value is invalid for this property.");
            properties.put(property, value);
            return this;
        }

        public ModelContext build() {
            return new ModelContext(Collections.unmodifiableMap(properties));
        }
    }



    public static class ModelProperty<T> implements Predicate<T> {
        private final Predicate<T> predicate;

        public ModelProperty() {
            this(Predicates.alwaysTrue());
        }

        public ModelProperty(Predicate<T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean test(T value) {
            return predicate.test(value);
        }
    }

    @Override
    public String toString() {
        List<String> entries = properties.entrySet().stream().map(e -> e.getValue().toString()).toList();
        return String.format("%s[%s]", getClass().getSimpleName(), String.join(", ", entries));
    }
}

