package de.mrjulsen.mcdragonlib.net.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Assembles a {@link DLStreamCodec} field by field.
 *
 * <p>Fields are written and read in the order they are declared and there is no limit on how
 * many a codec may have. The target type needs a way to create a blank instance plus a getter
 * and a setter per field. Immutable types are better served by
 * {@link DLStreamCodec#composite(DLStreamCodec, Function, DLStreamCodec, Function, java.util.function.BiFunction)}.
 *
 * @param <T> the value type being built
 */
public final class DLStreamCodecBuilder<T> {

    private interface Step<T> {
        void encode(FriendlyByteBuf buf, T value);

        void decode(FriendlyByteBuf buf, T value);
    }

    private final Supplier<T> factory;
    private final List<Step<T>> steps = new ArrayList<>();
    private boolean built;

    private DLStreamCodecBuilder(Supplier<T> factory) {
        this.factory = factory;
    }

    /**
     * Starts a new codec for a type that can be created blank and then populated.
     *
     * @param factory creates an empty instance while decoding
     * @param <T> the value type
     * @return a builder with no fields yet
     */
    public static <T> DLStreamCodecBuilder<T> of(Supplier<T> factory) {
        return new DLStreamCodecBuilder<>(Objects.requireNonNull(factory, "factory"));
    }

    /**
     * Appends a field.
     *
     * @param codec the codec handling the field value
     * @param getter reads the field from an instance
     * @param setter writes the field back into an instance
     * @param <F> the field type
     * @return this builder
     */
    public <F> DLStreamCodecBuilder<T> field(DLStreamCodec<F> codec, Function<T, F> getter, BiConsumer<T, F> setter) {
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(getter, "getter");
        Objects.requireNonNull(setter, "setter");
        steps.add(new Step<T>() {
            @Override
            public void encode(FriendlyByteBuf buf, T value) {
                codec.encode(buf, getter.apply(value));
            }

            @Override
            public void decode(FriendlyByteBuf buf, T value) {
                setter.accept(value, codec.decode(buf));
            }
        });
        return this;
    }

    /**
     * Appends a field that is read and written through the buffer directly, for cases no codec covers.
     *
     * @param encoder writes the field of an instance to the buffer
     * @param decoder reads the field from the buffer into an instance
     * @return this builder
     */
    public DLStreamCodecBuilder<T> raw(BiConsumer<FriendlyByteBuf, T> encoder, BiConsumer<FriendlyByteBuf, T> decoder) {
        Objects.requireNonNull(encoder, "encoder");
        Objects.requireNonNull(decoder, "decoder");
        steps.add(new Step<T>() {
            @Override
            public void encode(FriendlyByteBuf buf, T value) {
                encoder.accept(buf, value);
            }

            @Override
            public void decode(FriendlyByteBuf buf, T value) {
                decoder.accept(buf, value);
            }
        });
        return this;
    }

    /**
     * Finishes the codec. The builder cannot be reused afterwards.
     *
     * @return a codec covering all declared fields in declaration order
     */
    public DLStreamCodec<T> build() {
        if (built) {
            throw new IllegalStateException("This builder has already been built.");
        }
        built = true;
        List<Step<T>> finished = List.copyOf(steps);
        Supplier<T> instanceFactory = factory;
        return new DLStreamCodec<T>() {
            @Override
            public T decode(FriendlyByteBuf buf) {
                T value = Objects.requireNonNull(instanceFactory.get(), "codec factory returned null");
                for (Step<T> step : finished) {
                    step.decode(buf, value);
                }
                return value;
            }

            @Override
            public void encode(FriendlyByteBuf buf, T value) {
                for (Step<T> step : finished) {
                    step.encode(buf, value);
                }
            }
        };
    }
}
