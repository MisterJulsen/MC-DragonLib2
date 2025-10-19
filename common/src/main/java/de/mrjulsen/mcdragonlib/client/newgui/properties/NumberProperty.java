package de.mrjulsen.mcdragonlib.client.newgui.properties;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Supplier;

public class NumberProperty<T extends Number & Comparable<T>> extends Property<T> {
    private final Supplier<T> minSupplier;
    private final Supplier<T> maxSupplier;
    private final T minValue;
    private final T maxValue;

    public NumberProperty(T defaultValue) {
        this(defaultValue, findMinBound(defaultValue), findMaxBound(defaultValue));
    }

    public NumberProperty(T defaultValue, Supplier<T> min, Supplier<T> max) {
        super(defaultValue);
        this.minSupplier = min;
        this.maxSupplier = max;
        this.minValue = null;
        this.maxValue = null;
    }

    public NumberProperty(T defaultValue, T min, T max) {
        super(defaultValue);
        this.minSupplier = null;
        this.maxSupplier = null;
        this.minValue = min;
        this.maxValue = max;
    }

    public T set(T value) {
        Objects.requireNonNull(value);
        if (value.compareTo(min()) < 0) {
            value = min();
        } else if (value.compareTo(max()) > 0) {
            value = max();
        }
        return super.set(value);
    }

    public T min() {
        return minSupplier != null ? minSupplier.get() : minValue;
    }

    public T max() {
        return maxSupplier != null ? maxSupplier.get() : maxValue;
    }

    private static <E extends Number> E findMinBound(E typeInstance) {
        if (typeInstance instanceof Integer) return (E) Integer.valueOf(Integer.MIN_VALUE);
        if (typeInstance instanceof Double) return (E) Double.valueOf(Double.NEGATIVE_INFINITY);
        if (typeInstance instanceof Long) return (E) Long.valueOf(Long.MIN_VALUE);
        if (typeInstance instanceof Float) return (E) Float.valueOf(Float.NEGATIVE_INFINITY);
        if (typeInstance instanceof Short) return (E) Short.valueOf(Short.MIN_VALUE);
        if (typeInstance instanceof Byte) return (E) Byte.valueOf(Byte.MIN_VALUE);
        if (typeInstance instanceof BigInteger || typeInstance instanceof BigDecimal) {
            throw new IllegalArgumentException(typeInstance.getClass().getSimpleName() + " has no fixed bounds and cannot be initialized automatically.");
        }
        throw new IllegalArgumentException("Unknown Number type for automatic limit values: " + typeInstance.getClass().getName());
    }

    private static <E extends Number> E findMaxBound(E typeInstance) {
        if (typeInstance instanceof Integer) return (E) Integer.valueOf(Integer.MAX_VALUE);
        if (typeInstance instanceof Double) return (E) Double.valueOf(Double.POSITIVE_INFINITY);
        if (typeInstance instanceof Long) return (E) Long.valueOf(Long.MAX_VALUE);
        if (typeInstance instanceof Float) return (E) Float.valueOf(Float.POSITIVE_INFINITY);
        if (typeInstance instanceof Short) return (E) Short.valueOf(Short.MAX_VALUE);
        if (typeInstance instanceof Byte) return (E) Byte.valueOf(Byte.MAX_VALUE);
        if (typeInstance instanceof BigInteger || typeInstance instanceof BigDecimal) {
            throw new IllegalArgumentException(typeInstance.getClass().getSimpleName() + " has no fixed bounds and cannot be initialized automatically.");
        }
        throw new IllegalArgumentException("Unknown Number type for automatic limit values: " + typeInstance.getClass().getName());
    }
}
