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

package de.mrjulsen.mcdragonlib.util;

import java.util.Objects;

/**
 * Generic triple container for three values (note: spelled 'Tripple' in codebase).
 *
 * @param <A> first type
 * @param <B> second type
 * @param <C> third type
 */
public class Tripple<A, B, C> {

    protected A value1;
    protected B value2;
    protected C value3;

    protected Tripple(A value1, B value2, C value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public static <A, B, C> Tripple<A, B, C> of(A first, B second, C third) {
        return new Tripple<A, B, C>(first, second, third);
    }

    public A getFirst() { return value1; }
    public B getSecond() { return value2; }
    public C getThird() { return value3; }

    protected void setFirst(A value) {
        this.value1 = value;
    }

    protected void setSecond(B value) {
        this.value2 = value;
    }

    protected void setThird(C value) {
        this.value3 = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tripple other) {
            return getFirst().equals(other.getFirst()) &&
                getSecond().equals(other.getSecond())&&
                getThird().equals(other.getThird());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirst(), getSecond(), getThird());
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", getFirst(), getSecond(), getThird());
    }

    /**
     * Mutable variant that exposes setters for all three components.
     */
    public static class MutableTripple<A, B, C> extends Tripple<A, B, C> {

        public MutableTripple(A value1, B value2, C value3) {
            super(value1, value2, value3);
        }

        @Override
        public void setFirst(A value) {
            super.setFirst(value);
        }

        @Override
        public void setSecond(B value) {
            super.setSecond(value);
        }    
        
        @Override
        protected void setThird(C value) {
            super.setThird(value);
        }
    }
}