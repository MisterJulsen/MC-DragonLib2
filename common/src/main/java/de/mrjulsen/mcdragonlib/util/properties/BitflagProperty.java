package de.mrjulsen.mcdragonlib.util.properties;

import java.lang.reflect.Array;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.BitflagEnum;

/**
 * A property implementation that stores an array of enum constants as a compact
 * bitfield (serialized as a {@link Long}).
 *
 * <p>The enum type {@code T} must implement {@link BitflagEnum} which provides
 * a numeric bit mask for each enum constant. This class converts between the
 * enum set representation (T[]) and a packed long mask for storage.</p>
 *
 * @param <T> the enum type which must implement {@link BitflagEnum}
 */
public class BitflagProperty<T extends Enum<T> & BitflagEnum> extends AbstractSerializableProperty<T[], Long> {
    private final Class<T> type;
        
    /**
     * Create a new BitflagProperty with the provided default values.
     *
     * @param type the enum class represented by this property
     * @param defaultValue the default enum constants (may be empty)
     */
    @SafeVarargs
    public BitflagProperty(Class<T> type, T... defaultValue) {
        super(defaultValue);
        this.type = type;
    }

    /**
     * Check whether the effective (possibly inherited) bitfield contains the given enum value.
     *
     * <p>If {@code value} is {@code null} this method returns {@link #isEmpty()}.</p>
     *
     * @param value the enum constant to test, or {@code null}
     * @return {@code true} if the corresponding bit is set in the effective value
     */
    public boolean has(T value) {
        if (value == null) {
            return isEmpty();
        }
        return (getSerializedCurrentValue() & value.getBit()) != 0;
    }

    /**
     * Check whether any of the provided enum constants is present in the effective bitfield.
     *
     * @param values the enum constants to test
     * @return {@code true} if at least one provided constant is present
     * @throws NullPointerException if {@code values} is {@code null}
     */
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

    /**
     * Check whether the effective bitfield contains only the provided enum constants
     * (no other bits set).
     *
     * @param values the enum constants that must exclusively be present
     * @return {@code true} if the effective bitfield exactly matches the provided set
     * @throws NullPointerException if {@code values} is {@code null}
     */
    @SafeVarargs
    public final boolean hasOnly(T... values) {
        Objects.requireNonNull(values);
        int combined = 0;
        for (T value : values) {
            combined |= value.getBit();
        }
        return getSerializedCurrentValue() == combined;
    }


    /**
     * Check whether all provided enum constants are present in the effective bitfield.
     *
     * @param values the enum constants to test
     * @return {@code true} if every provided constant's bit is set
     * @throws NullPointerException if {@code values} is {@code null}
     */
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

    /**
     * Check whether none of the provided enum constants are present in the local stored bitfield.
     *
     * <p>Note: this method checks the local stored value (not the effective/inherited value).</p>
     *
     * @param values the enum constants to test
     * @return {@code true} if none of the provided bits are set locally
     * @throws NullPointerException if {@code values} is {@code null}
     */
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
    
    /**
     * Check whether any enum constant that matches the supplied predicate is present
     * in the effective bitfield.
     *
     * @param predicate a predicate to test enum constants
     * @return {@code true} if at least one matching constant is set
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    public boolean hasMatching(Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        for (T constant : type.getEnumConstants()) {
            if ((getSerializedCurrentValue() & constant.getBit()) != 0 && predicate.test(constant)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return whether the effective bitfield is empty (no bits set).
     *
     * @return {@code true} if no bits are set in the effective value
     */
    public boolean isEmpty() {
        return getSerializedCurrentValue() == 0;
    }
    
    /**
     * Return whether the effective bitfield contains exactly one bit.
     *
     * @return {@code true} if exactly one flag is set in the effective value
     */
    public boolean isSingle() {
        return Long.bitCount(getSerializedCurrentValue()) == 1;
    }
    
    /**
     * Alias for {@link #set set(T...)} that returns the applied array.
     *
     * @param values the enum constants to set
     * @return the input array {@code values}
     */
    @SafeVarargs
    public final T[] set2(T... values) {
        return set(values);
    }

    /**
     * Convenience overload that accepts an {@link EnumSet} and sets the property to its contents.
     *
     * @param t the EnumSet to set
     * @return the applied array of enum constants
     */
    @SuppressWarnings("unchecked")
    public T[] set(EnumSet<T> t) {
        return set(t.toArray((T[])Array.newInstance(type, t.size())));
    }
    
    /**
     * Add the provided enum constants to the local stored value.
     *
     * <p>The modification callback (if registered) is applied to the combined result,
     * and the after-change callback is invoked afterwards.</p>
     *
     * @param t enum constants to add
     */
    @SafeVarargs
    public final void add(T... t) {
        T[] old = get();
        T[] val = getModificationCallback().map(x -> x.update(old, t)).orElse(t);
        serializeInto(getSerializedValue(), val);
        getAfterChangeCallback().ifPresent(x -> x.update(old, val));
    }

    /**
     * Serialize the provided enum array into a {@link Long} bit mask.
     *
     * @param t the enum constants to serialize
     * @return the packed long bit mask representing the provided constants
     */
    @Override
    protected Long serialize(T[] t) {
        return serializeInto(0, t);
    }

    /**
     * Set bits in the provided flags long for each enum constant in {@code t}.
     *
     * <p>This method is protected and may be used by subclasses to merge flags into
     * an existing bitfield.</p>
     *
     * @param flags the initial flags value to OR into
     * @param t the enum constants whose bits should be set
     * @return the resulting flags after OR-ing in the provided constants
     */
    @SafeVarargs
    protected final long serializeInto(long flags, T... t) {
        for (T v : t) {
            flags |= v.getBit();
        }
        return flags;
    }

    /**
     * Deserialize the provided {@link Long} bit mask into an array of enum constants.
     *
     * @param flags the packed long bit mask
     * @return an array containing the enum constants represented by {@code flags}
     */
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
