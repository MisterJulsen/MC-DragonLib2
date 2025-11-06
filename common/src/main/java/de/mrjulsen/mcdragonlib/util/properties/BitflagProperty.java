package de.mrjulsen.mcdragonlib.util.properties;

import java.lang.reflect.Array;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.BitflagEnum;

public class BitflagProperty<T extends Enum<T> & BitflagEnum> extends AbstractSerializableProperty<T[], Long> {
    private final Class<T> type;
        
    @SafeVarargs
    public BitflagProperty(Class<T> type, T... defaultValue) {
        super(defaultValue);
        this.type = type;
    }

    public boolean has(T value) {
        if (value == null) {
            return isEmpty();
        }
        return (getSerializedCurrentValue() & value.getBit()) != 0;
    }

    @SafeVarargs
    public final boolean hasAny(T... values) {
        Objects.requireNonNull(values);
        for (T value : values) {
            if ((getSerializedCurrentValue() & value.getBit()) != 0) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public final boolean hasOnly(T... values) {
        Objects.requireNonNull(values);
        int combined = 0;
        for (T value : values) {
            combined |= value.getBit();
        }
        return getSerializedCurrentValue() == combined;
    }


    @SafeVarargs
    public final boolean hasAll(T... values) {
        Objects.requireNonNull(values);
        for (T value : values) {
            if ((getSerializedCurrentValue() & value.getBit()) == 0) {
                return false;
            }
        }
        return true;
    }

    @SafeVarargs
    public final boolean hasNone(T... values) {
        Objects.requireNonNull(values);
        for (T value : values) {
            if ((getSerializedValue() & value.getBit()) != 0) {
                return false;
            }
        }
        return true;
    }
    
    public boolean hasMatching(Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        for (T constant : type.getEnumConstants()) {
            if ((getSerializedCurrentValue() & constant.getBit()) != 0 && predicate.test(constant)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return getSerializedCurrentValue() == 0;
    }
    
    public boolean isSingle() {
        return Long.bitCount(getSerializedCurrentValue()) == 1;
    }
    
    @SafeVarargs
    public final T[] set2(T... values) {
        return set(values);
    }

    @SuppressWarnings("unchecked")
    public T[] set(EnumSet<T> t) {
        return set(t.toArray((T[])Array.newInstance(type, t.size())));
    }
    
    @SafeVarargs
    public final void add(T... t) {
        T[] old = get();
        T[] val = getModificationCallback().map(x -> x.update(old, t)).orElse(t);
        serializeInto(getSerializedValue(), val);
        getAfterChangeCallback().ifPresent(x -> x.update(old, val));
    }

    @Override
    protected Long serialize(T[] t) {
        return serializeInto(0, t);
    }

    @SafeVarargs
    protected final long serializeInto(long flags, T... t) {
        for (T v : t) {
            flags |= v.getBit();
        }
        return flags;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T[] deserialize(Long flags) {
        EnumSet<T> set = EnumSet.noneOf(type);
        for (T constant : type.getEnumConstants()) {
            if ((flags & constant.getBit()) != 0) {
                set.add(constant);
            }
        }
        return set.toArray((T[])Array.newInstance(type, set.size()));
    }
}
