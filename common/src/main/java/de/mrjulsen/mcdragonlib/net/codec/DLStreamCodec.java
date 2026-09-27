package de.mrjulsen.mcdragonlib.net.codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Reads and writes a value directly from and to a network buffer.
 *
 * <p>Unlike a {@code Codec}, no intermediate tree is built, which makes this suitable for
 * large payloads. Instances are stateless and must be safe to use from any thread.
 *
 * @param <T> the value type handled by this codec
 */
public interface DLStreamCodec<T> {

    /**
     * Reads a single value from the buffer.
     *
     * @param buf the buffer positioned at the start of the value
     * @return the decoded value
     */
    T decode(FriendlyByteBuf buf);

    /**
     * Writes a single value to the buffer.
     *
     * @param buf the buffer to append to
     * @param value the value to write
     */
    void encode(FriendlyByteBuf buf, T value);

    /**
     * Creates a codec from a write and a read function.
     *
     * @param encoder writes a value to the buffer
     * @param decoder reads a value from the buffer
     * @param <T> the value type
     * @return a codec backed by the given functions
     */
    static <T> DLStreamCodec<T> of(BiConsumer<FriendlyByteBuf, T> encoder, Function<FriendlyByteBuf, T> decoder) {
        Objects.requireNonNull(encoder, "encoder");
        Objects.requireNonNull(decoder, "decoder");
        return new DLStreamCodec<T>() {
            @Override
            public T decode(FriendlyByteBuf buf) {
                return decoder.apply(buf);
            }

            @Override
            public void encode(FriendlyByteBuf buf, T value) {
                encoder.accept(buf, value);
            }
        };
    }

    /**
     * Creates a codec that writes nothing and always decodes the same value.
     *
     * @param value the constant value
     * @param <T> the value type
     * @return a zero-byte codec
     */
    static <T> DLStreamCodec<T> unit(T value) {
        return of((buf, v) -> {}, buf -> value);
    }

    /**
     * Defers resolution of the delegate until first use, which allows recursive or
     * forward-referencing codec definitions.
     *
     * @param delegate supplies the actual codec
     * @param <T> the value type
     * @return a codec resolving to the supplied delegate
     */
    static <T> DLStreamCodec<T> lazy(Supplier<DLStreamCodec<T>> delegate) {
        Objects.requireNonNull(delegate, "delegate");
        return new DLStreamCodec<T>() {
            private volatile DLStreamCodec<T> resolved;

            private DLStreamCodec<T> resolve() {
                DLStreamCodec<T> codec = resolved;
                if (codec == null) {
                    codec = Objects.requireNonNull(delegate.get(), "lazy codec resolved to null");
                    resolved = codec;
                }
                return codec;
            }

            @Override
            public T decode(FriendlyByteBuf buf) {
                return resolve().decode(buf);
            }

            @Override
            public void encode(FriendlyByteBuf buf, T value) {
                resolve().encode(buf, value);
            }
        };
    }

    /**
     * Selects the codec to use per value, which allows a single codec to cover a type hierarchy.
     *
     * @param keyCodec codec for the discriminator written ahead of the value
     * @param keyOf extracts the discriminator from a value
     * @param lookup resolves a discriminator to the codec handling it
     * @param <T> the common value type
     * @param <K> the discriminator type
     * @return a codec dispatching on the discriminator
     */
    @SuppressWarnings("unchecked")
    static <T, K> DLStreamCodec<T> dispatch(DLStreamCodec<K> keyCodec, Function<T, K> keyOf, Function<K, DLStreamCodec<? extends T>> lookup) {
        Objects.requireNonNull(keyCodec, "keyCodec");
        Objects.requireNonNull(keyOf, "keyOf");
        Objects.requireNonNull(lookup, "lookup");
        return new DLStreamCodec<T>() {
            @Override
            public T decode(FriendlyByteBuf buf) {
                K key = keyCodec.decode(buf);
                DLStreamCodec<? extends T> codec = lookup.apply(key);
                if (codec == null) {
                    throw new IllegalStateException("No codec registered for key " + key + ".");
                }
                return codec.decode(buf);
            }

            @Override
            public void encode(FriendlyByteBuf buf, T value) {
                K key = keyOf.apply(value);
                DLStreamCodec<? extends T> codec = lookup.apply(key);
                if (codec == null) {
                    throw new IllegalStateException("No codec registered for key " + key + ".");
                }
                keyCodec.encode(buf, key);
                ((DLStreamCodec<T>) codec).encode(buf, value);
            }
        };
    }

    /**
     * Converts this codec to a different representation.
     *
     * @param to builds the target value from a decoded one
     * @param from extracts the encodable value from a target one
     * @param <R> the target type
     * @return a codec for the target type
     */
    default <R> DLStreamCodec<R> map(Function<T, R> to, Function<R, T> from) {
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(from, "from");
        return of((buf, value) -> encode(buf, from.apply(value)), buf -> to.apply(decode(buf)));
    }

    /**
     * Wraps this codec so that a missing value is represented by an empty optional.
     *
     * @return a codec for an optional value
     */
    default DLStreamCodec<Optional<T>> optional() {
        return of(
            (buf, value) -> {
                buf.writeBoolean(value.isPresent());
                value.ifPresent(v -> encode(buf, v));
            },
            buf -> buf.readBoolean() ? Optional.of(decode(buf)) : Optional.empty()
        );
    }

    /**
     * Wraps this codec so that {@code null} becomes a valid value.
     *
     * @return a codec accepting and producing {@code null}
     */
    default DLStreamCodec<T> nullable() {
        return of(
            (buf, value) -> {
                buf.writeBoolean(value != null);
                if (value != null) {
                    encode(buf, value);
                }
            },
            buf -> buf.readBoolean() ? decode(buf) : null
        );
    }

    /**
     * Creates a codec for a list of values of this type.
     *
     * @return a codec for an unbounded list
     */
    default DLStreamCodec<List<T>> list() {
        return list(Integer.MAX_VALUE);
    }

    /**
     * Creates a codec for a list of values of this type, rejecting oversized lists while decoding.
     *
     * @param maxSize the highest accepted element count
     * @return a codec for a bounded list
     */
    default DLStreamCodec<List<T>> list(int maxSize) {
        return collection(ArrayList::new, maxSize);
    }

    /**
     * Creates a codec for an arbitrary collection of values of this type.
     *
     * @param factory creates the target collection for a known element count
     * @param <C> the collection type
     * @return a codec for the collection
     */
    default <C extends Collection<T>> DLStreamCodec<C> collection(IntFunction<C> factory) {
        return collection(factory, Integer.MAX_VALUE);
    }

    /**
     * Creates a codec for an arbitrary collection of values of this type, rejecting oversized
     * collections while decoding.
     *
     * @param factory creates the target collection for a known element count
     * @param maxSize the highest accepted element count
     * @param <C> the collection type
     * @return a codec for the collection
     */
    default <C extends Collection<T>> DLStreamCodec<C> collection(IntFunction<C> factory, int maxSize) {
        Objects.requireNonNull(factory, "factory");
        return of(
            (buf, values) -> {
                buf.writeVarInt(values.size());
                for (T value : values) {
                    encode(buf, value);
                }
            },
            buf -> {
                int size = buf.readVarInt();
                if (size < 0 || size > maxSize) {
                    throw new IllegalStateException("Collection size " + size + " exceeds the allowed maximum of " + maxSize + ".");
                }
                C values = factory.apply(size);
                for (int i = 0; i < size; i++) {
                    values.add(decode(buf));
                }
                return values;
            }
        );
    }

    /**
     * Encodes a value into a standalone byte array.
     *
     * @param value the value to encode
     * @return the encoded bytes
     */
    default byte[] toBytes(T value) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            encode(buf, value);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return bytes;
        } finally {
            buf.release();
        }
    }

    /**
     * Decodes a value from a standalone byte array.
     *
     * @param bytes the encoded bytes
     * @return the decoded value
     */
    default T fromBytes(byte[] bytes) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        try {
            return decode(buf);
        } finally {
            buf.release();
        }
    }

    /**
     * Combines two field codecs into a codec for a composite value.
     *
     * @return a codec writing the fields in the given order
     */
    static <T, T1, T2> DLStreamCodec<T> composite(
        DLStreamCodec<T1> codec1, Function<T, T1> field1,
        DLStreamCodec<T2> codec2, Function<T, T2> field2,
        BiFunction<T1, T2, T> factory
    ) {
        return of(
            (buf, value) -> {
                codec1.encode(buf, field1.apply(value));
                codec2.encode(buf, field2.apply(value));
            },
            buf -> factory.apply(codec1.decode(buf), codec2.decode(buf))
        );
    }

    /**
     * Combines three field codecs into a codec for a composite value.
     *
     * @return a codec writing the fields in the given order
     */
    static <T, T1, T2, T3> DLStreamCodec<T> composite(
        DLStreamCodec<T1> codec1, Function<T, T1> field1,
        DLStreamCodec<T2> codec2, Function<T, T2> field2,
        DLStreamCodec<T3> codec3, Function<T, T3> field3,
        Function3<T1, T2, T3, T> factory
    ) {
        return of(
            (buf, value) -> {
                codec1.encode(buf, field1.apply(value));
                codec2.encode(buf, field2.apply(value));
                codec3.encode(buf, field3.apply(value));
            },
            buf -> factory.apply(codec1.decode(buf), codec2.decode(buf), codec3.decode(buf))
        );
    }

    /**
     * Combines four field codecs into a codec for a composite value.
     *
     * @return a codec writing the fields in the given order
     */
    static <T, T1, T2, T3, T4> DLStreamCodec<T> composite(
        DLStreamCodec<T1> codec1, Function<T, T1> field1,
        DLStreamCodec<T2> codec2, Function<T, T2> field2,
        DLStreamCodec<T3> codec3, Function<T, T3> field3,
        DLStreamCodec<T4> codec4, Function<T, T4> field4,
        Function4<T1, T2, T3, T4, T> factory
    ) {
        return of(
            (buf, value) -> {
                codec1.encode(buf, field1.apply(value));
                codec2.encode(buf, field2.apply(value));
                codec3.encode(buf, field3.apply(value));
                codec4.encode(buf, field4.apply(value));
            },
            buf -> factory.apply(codec1.decode(buf), codec2.decode(buf), codec3.decode(buf), codec4.decode(buf))
        );
    }

    /**
     * Combines five field codecs into a codec for a composite value.
     *
     * @return a codec writing the fields in the given order
     */
    static <T, T1, T2, T3, T4, T5> DLStreamCodec<T> composite(
        DLStreamCodec<T1> codec1, Function<T, T1> field1,
        DLStreamCodec<T2> codec2, Function<T, T2> field2,
        DLStreamCodec<T3> codec3, Function<T, T3> field3,
        DLStreamCodec<T4> codec4, Function<T, T4> field4,
        DLStreamCodec<T5> codec5, Function<T, T5> field5,
        Function5<T1, T2, T3, T4, T5, T> factory
    ) {
        return of(
            (buf, value) -> {
                codec1.encode(buf, field1.apply(value));
                codec2.encode(buf, field2.apply(value));
                codec3.encode(buf, field3.apply(value));
                codec4.encode(buf, field4.apply(value));
                codec5.encode(buf, field5.apply(value));
            },
            buf -> factory.apply(codec1.decode(buf), codec2.decode(buf), codec3.decode(buf), codec4.decode(buf), codec5.decode(buf))
        );
    }

    /**
     * Combines six field codecs into a codec for a composite value.
     *
     * @return a codec writing the fields in the given order
     */
    static <T, T1, T2, T3, T4, T5, T6> DLStreamCodec<T> composite(
        DLStreamCodec<T1> codec1, Function<T, T1> field1,
        DLStreamCodec<T2> codec2, Function<T, T2> field2,
        DLStreamCodec<T3> codec3, Function<T, T3> field3,
        DLStreamCodec<T4> codec4, Function<T, T4> field4,
        DLStreamCodec<T5> codec5, Function<T, T5> field5,
        DLStreamCodec<T6> codec6, Function<T, T6> field6,
        Function6<T1, T2, T3, T4, T5, T6, T> factory
    ) {
        return of(
            (buf, value) -> {
                codec1.encode(buf, field1.apply(value));
                codec2.encode(buf, field2.apply(value));
                codec3.encode(buf, field3.apply(value));
                codec4.encode(buf, field4.apply(value));
                codec5.encode(buf, field5.apply(value));
                codec6.encode(buf, field6.apply(value));
            },
            buf -> factory.apply(codec1.decode(buf), codec2.decode(buf), codec3.decode(buf), codec4.decode(buf), codec5.decode(buf), codec6.decode(buf))
        );
    }

    /**
     * Combines seven field codecs into a codec for a composite value.
     *
     * @return a codec writing the fields in the given order
     */
    static <T, T1, T2, T3, T4, T5, T6, T7> DLStreamCodec<T> composite(
        DLStreamCodec<T1> codec1, Function<T, T1> field1,
        DLStreamCodec<T2> codec2, Function<T, T2> field2,
        DLStreamCodec<T3> codec3, Function<T, T3> field3,
        DLStreamCodec<T4> codec4, Function<T, T4> field4,
        DLStreamCodec<T5> codec5, Function<T, T5> field5,
        DLStreamCodec<T6> codec6, Function<T, T6> field6,
        DLStreamCodec<T7> codec7, Function<T, T7> field7,
        Function7<T1, T2, T3, T4, T5, T6, T7, T> factory
    ) {
        return of(
            (buf, value) -> {
                codec1.encode(buf, field1.apply(value));
                codec2.encode(buf, field2.apply(value));
                codec3.encode(buf, field3.apply(value));
                codec4.encode(buf, field4.apply(value));
                codec5.encode(buf, field5.apply(value));
                codec6.encode(buf, field6.apply(value));
                codec7.encode(buf, field7.apply(value));
            },
            buf -> factory.apply(codec1.decode(buf), codec2.decode(buf), codec3.decode(buf), codec4.decode(buf), codec5.decode(buf), codec6.decode(buf), codec7.decode(buf))
        );
    }

    /**
     * Combines eight field codecs into a codec for a composite value. For more fields either nest
     * composites or use {@link DLStreamCodecBuilder}, which has no field limit.
     *
     * @return a codec writing the fields in the given order
     */
    static <T, T1, T2, T3, T4, T5, T6, T7, T8> DLStreamCodec<T> composite(
        DLStreamCodec<T1> codec1, Function<T, T1> field1,
        DLStreamCodec<T2> codec2, Function<T, T2> field2,
        DLStreamCodec<T3> codec3, Function<T, T3> field3,
        DLStreamCodec<T4> codec4, Function<T, T4> field4,
        DLStreamCodec<T5> codec5, Function<T, T5> field5,
        DLStreamCodec<T6> codec6, Function<T, T6> field6,
        DLStreamCodec<T7> codec7, Function<T, T7> field7,
        DLStreamCodec<T8> codec8, Function<T, T8> field8,
        Function8<T1, T2, T3, T4, T5, T6, T7, T8, T> factory
    ) {
        return of(
            (buf, value) -> {
                codec1.encode(buf, field1.apply(value));
                codec2.encode(buf, field2.apply(value));
                codec3.encode(buf, field3.apply(value));
                codec4.encode(buf, field4.apply(value));
                codec5.encode(buf, field5.apply(value));
                codec6.encode(buf, field6.apply(value));
                codec7.encode(buf, field7.apply(value));
                codec8.encode(buf, field8.apply(value));
            },
            buf -> factory.apply(codec1.decode(buf), codec2.decode(buf), codec3.decode(buf), codec4.decode(buf), codec5.decode(buf), codec6.decode(buf), codec7.decode(buf), codec8.decode(buf))
        );
    }
}
