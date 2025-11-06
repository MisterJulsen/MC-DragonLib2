package de.mrjulsen.mcdragonlib.util;

import java.util.Objects;

/**
 * A simple wrapper for all kinds of objects.
 */
public class Holder<A> {
    protected A value1;

    public Holder(A value1) {
        this.value1 = value1;
    }

    public static <A> Holder<A>of(A first) {
        return new Holder<A>(first);
    }

    public A get() {
        return value1;
    }

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
     * A simple wrapper for all kinds of objects. The contents of this object can be changed at any time.
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