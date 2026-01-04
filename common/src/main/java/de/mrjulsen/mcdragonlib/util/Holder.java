package de.mrjulsen.mcdragonlib.util;

import java.util.Objects;

/**
 * Simple generic immutable wrapper for a single value.
 *
 * <p>Provides convenience factory and equality semantics. Use {@link MutableHolder} for a mutable variant.
 */
public class Holder<A> {
    protected A value1;

    /**
     * Create a new Holder with the provided value.
     */
    public Holder(A value1) {
        this.value1 = value1;
    }

    /**
     * Factory helper.
     */
    public static <A> Holder<A> of(A first) {
        return new Holder<A>(first);
    }

    /**
     * Return wrapped value.
     */
    public A get() {
        return value1;
    }

    /**
     * Protected setter for subclasses.
     */
    protected void set(A value) {
        this.value1 = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Holder other) {
            return get().equals(other.get());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }

    @Override
    public String toString() {
        return String.format("(%s)", get());
    }
    
    /**
     * Mutable variant that exposes a public setter.
     */
    public static class MutableHolder<A> extends Holder<A> {

        public MutableHolder(A value1) {
            super(value1);
        }

        @Override
        public void set(A value) {
            super.set(value);
        }     
    }
}