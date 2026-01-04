package de.mrjulsen.mcdragonlib.util;

import java.util.Objects;

/**
 * Generic pair container for two values with convenience factory and equality semantics.
 *
 * @param <A> first type
 * @param <B> second type
 */
public class Pair<A, B> {
    protected A value1;
    protected B value2;

    /**
     * Create a new Pair.
     */
    public Pair(A value1, B value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Factory helper.
     */
    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<A, B>(first, second);
    }

    /**
     * Return first value.
     */
    public A getFirst() {
        return value1;
    }

    /**
     * Return second value.
     */
    public B getSecond() {
        return value2;
    }

    /**
     * Protected setter for subclasses.
     */
    protected void setFirst(A value) {
        this.value1 = value;
    }

    /**
     * Protected setter for subclasses.
     */
    protected void setSecond(B value) {
        this.value2 = value;
    }

    /**
     * Return a swapped pair (utility expecting another pair).
     */
    public Pair<A, B> swap(Pair<A, B> pair) {
        return Pair.of(pair.getFirst(), pair.getSecond());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair other) {
            return getFirst().equals(other.getFirst()) && getSecond().equals(other.getSecond());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirst(), getSecond());
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", getFirst(), getSecond());
    }

    /**
     * Mutable variant of Pair exposing protected setters as public.
     */
    public static class MutablePair<A, B> extends Pair<A, B> {

        public MutablePair(A value1, B value2) {
            super(value1, value2);
        }

        @Override
        protected void setFirst(A value) {
            super.setFirst(value);
        }

        @Override
        public void setSecond(B value) {
            super.setSecond(value);
        }        
    }
}