package de.mrjulsen.mcdragonlib.net.codec;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Ready-made {@link DLStreamCodec} instances for common value types, plus bridges to the
 * serialization mechanisms Minecraft and DragonLib already use.
 */
public final class DLCodecs {

    private static final String WRAPPED_VALUE_KEY = "Value";

    private DLCodecs() {}

    /** Codec for a single boolean. */
    public static final DLStreamCodec<Boolean> BOOL = DLStreamCodec.of((buf, value) -> buf.writeBoolean(value), buf -> buf.readBoolean());
    /** Codec for a single byte. */
    public static final DLStreamCodec<Byte> BYTE = DLStreamCodec.of((buf, value) -> buf.writeByte(value), buf -> buf.readByte());
    /** Codec for a single short. */
    public static final DLStreamCodec<Short> SHORT = DLStreamCodec.of((buf, value) -> buf.writeShort(value), buf -> buf.readShort());
    /** Codec for a fixed-width integer. */
    public static final DLStreamCodec<Integer> INT = DLStreamCodec.of((buf, value) -> buf.writeInt(value), buf -> buf.readInt());
    /** Codec for a variable-length integer, which is shorter for small values. */
    public static final DLStreamCodec<Integer> VAR_INT = DLStreamCodec.of((buf, value) -> buf.writeVarInt(value), buf -> buf.readVarInt());
    /** Codec for a fixed-width long. */
    public static final DLStreamCodec<Long> LONG = DLStreamCodec.of((buf, value) -> buf.writeLong(value), buf -> buf.readLong());
    /** Codec for a variable-length long, which is shorter for small values. */
    public static final DLStreamCodec<Long> VAR_LONG = DLStreamCodec.of((buf, value) -> buf.writeVarLong(value), buf -> buf.readVarLong());
    /** Codec for a single float. */
    public static final DLStreamCodec<Float> FLOAT = DLStreamCodec.of((buf, value) -> buf.writeFloat(value), buf -> buf.readFloat());
    /** Codec for a single double. */
    public static final DLStreamCodec<Double> DOUBLE = DLStreamCodec.of((buf, value) -> buf.writeDouble(value), buf -> buf.readDouble());
    /** Codec for a string of up to 32767 characters. */
    public static final DLStreamCodec<String> STRING = string(32767);
    /** Codec for a raw byte array. */
    public static final DLStreamCodec<byte[]> BYTE_ARRAY = DLStreamCodec.of((buf, value) -> buf.writeByteArray(value), buf -> buf.readByteArray());
    /** Codec for a UUID. */
    public static final DLStreamCodec<UUID> UUID_CODEC = DLStreamCodec.of((buf, value) -> buf.writeUUID(value), buf -> buf.readUUID());
    /** Codec for a resource location. */
    public static final DLStreamCodec<ResourceLocation> RESOURCE_LOCATION = DLStreamCodec.of((buf, value) -> buf.writeResourceLocation(value), buf -> buf.readResourceLocation());
    /** Codec for a compound tag of unrestricted size. */
    public static final DLStreamCodec<CompoundTag> NBT = DLStreamCodec.of((buf, value) -> buf.writeNbt(value), buf -> buf.readAnySizeNbt());
    /** Codec for an item stack. */
    public static final DLStreamCodec<ItemStack> ITEM_STACK = DLStreamCodec.of((buf, value) -> buf.writeItem(value), buf -> buf.readItem());
    /** Codec for a block position. */
    public static final DLStreamCodec<BlockPos> BLOCK_POS = DLStreamCodec.of((buf, value) -> buf.writeBlockPos(value), buf -> buf.readBlockPos());
    /** Codec for a dimension key. */
    public static final DLStreamCodec<ResourceKey<Level>> DIMENSION = DLStreamCodec.of(
        (buf, value) -> buf.writeResourceLocation(value.location()),
        buf -> ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation())
    );
    /** Codec for a position including its dimension. */
    public static final DLStreamCodec<GlobalPos> GLOBAL_POS = DLStreamCodec.of(
        (buf, value) -> {
            buf.writeResourceLocation(value.dimension().location());
            buf.writeBlockPos(value.pos());
        },
        buf -> GlobalPos.of(ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()), buf.readBlockPos())
    );
    /** Codec for a text component. */
    public static final DLStreamCodec<Component> COMPONENT = DLStreamCodec.of((buf, value) -> buf.writeComponent(value), buf -> buf.readComponent());
    /** Codec for a three-dimensional vector. */
    public static final DLStreamCodec<Vec3> VEC3 = DLStreamCodec.of(
        (buf, value) -> {
            buf.writeDouble(value.x);
            buf.writeDouble(value.y);
            buf.writeDouble(value.z);
        },
        buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    /**
     * Creates a codec for a string with a decoding length limit.
     *
     * @param maxLength the highest accepted character count
     * @return a codec for a bounded string
     */
    public static DLStreamCodec<String> string(int maxLength) {
        return DLStreamCodec.of((buf, value) -> buf.writeUtf(value, maxLength), buf -> buf.readUtf(maxLength));
    }

    /**
     * Creates a codec for a byte array with a decoding length limit.
     *
     * @param maxLength the highest accepted byte count
     * @return a codec for a bounded byte array
     */
    public static DLStreamCodec<byte[]> byteArray(int maxLength) {
        return DLStreamCodec.of((buf, value) -> buf.writeByteArray(value), buf -> buf.readByteArray(maxLength));
    }

    /**
     * Creates a codec writing an enum constant by ordinal.
     *
     * <p>This is the compact option but it ties the wire format to the declaration order: inserting
     * a constant anywhere but at the end silently shifts every constant after it. Prefer
     * {@link #enumByName(Class)} unless the enum is fixed or the size really matters.
     *
     * @param type the enum class
     * @param <E> the enum type
     * @return a codec writing the position of the constant
     */
    public static <E extends Enum<E>> DLStreamCodec<E> enumByOrdinal(Class<E> type) {
        Objects.requireNonNull(type, "type");
        E[] constants = type.getEnumConstants();
        return DLStreamCodec.of(
            (buf, value) -> buf.writeVarInt(value.ordinal()),
            buf -> {
                int ordinal = buf.readVarInt();
                if (ordinal < 0 || ordinal >= constants.length) {
                    throw new IllegalStateException("Unknown ordinal " + ordinal + " for enum " + type.getSimpleName() + ".");
                }
                return constants[ordinal];
            }
        );
    }

    /**
     * Creates a codec writing an enum constant by name, which survives reordering and insertion of
     * constants.
     *
     * @param type the enum class
     * @param <E> the enum type
     * @return a codec writing the name of the constant
     */
    public static <E extends Enum<E>> DLStreamCodec<E> enumByName(Class<E> type) {
        Objects.requireNonNull(type, "type");
        return DLStreamCodec.of(
            (buf, value) -> buf.writeUtf(value.name()),
            buf -> Enum.valueOf(type, buf.readUtf())
        );
    }

    /**
     * Creates a codec for a set of enum constants.
     *
     * @param type the enum class
     * @param elementCodec how a single constant is written, normally {@link #enumByName(Class)} or
     *        {@link #enumByOrdinal(Class)}
     * @param <E> the enum type
     * @return a codec for an enum set
     */
    public static <E extends Enum<E>> DLStreamCodec<EnumSet<E>> enumSet(Class<E> type, DLStreamCodec<E> elementCodec) {
        Objects.requireNonNull(type, "type");
        return elementCodec.collection(size -> EnumSet.noneOf(type));
    }

    /**
     * Creates a codec for a map, preserving iteration order on the receiving side.
     *
     * @param keyCodec codec for the keys
     * @param valueCodec codec for the values
     * @param <K> the key type
     * @param <V> the value type
     * @return a codec for an unbounded map
     */
    public static <K, V> DLStreamCodec<Map<K, V>> map(DLStreamCodec<K> keyCodec, DLStreamCodec<V> valueCodec) {
        return map(keyCodec, valueCodec, size -> new LinkedHashMap<>(), Integer.MAX_VALUE);
    }

    /**
     * Creates a codec for a map with a decoding size limit.
     *
     * @param keyCodec codec for the keys
     * @param valueCodec codec for the values
     * @param factory creates the target map for a known entry count
     * @param maxSize the highest accepted entry count
     * @param <K> the key type
     * @param <V> the value type
     * @param <M> the map type
     * @return a codec for a bounded map
     */
    public static <K, V, M extends Map<K, V>> DLStreamCodec<M> map(DLStreamCodec<K> keyCodec, DLStreamCodec<V> valueCodec, IntFunction<M> factory, int maxSize) {
        Objects.requireNonNull(keyCodec, "keyCodec");
        Objects.requireNonNull(valueCodec, "valueCodec");
        Objects.requireNonNull(factory, "factory");
        return DLStreamCodec.of(
            (buf, value) -> {
                buf.writeVarInt(value.size());
                for (Map.Entry<K, V> entry : value.entrySet()) {
                    keyCodec.encode(buf, entry.getKey());
                    valueCodec.encode(buf, entry.getValue());
                }
            },
            buf -> {
                int size = buf.readVarInt();
                if (size < 0 || size > maxSize) {
                    throw new IllegalStateException("Map size " + size + " exceeds the allowed maximum of " + maxSize + ".");
                }
                M values = factory.apply(size);
                for (int i = 0; i < size; i++) {
                    values.put(keyCodec.decode(buf), valueCodec.decode(buf));
                }
                return values;
            }
        );
    }

    /**
     * Creates a map codec backed by a plain hash map.
     *
     * @param keyCodec codec for the keys
     * @param valueCodec codec for the values
     * @param <K> the key type
     * @param <V> the value type
     * @return a codec for an unordered map
     */
    public static <K, V> DLStreamCodec<Map<K, V>> hashMap(DLStreamCodec<K> keyCodec, DLStreamCodec<V> valueCodec) {
        return map(keyCodec, valueCodec, size -> new HashMap<>(), Integer.MAX_VALUE);
    }

    /**
     * Adapts an existing data-fixer codec by routing it through NBT.
     *
     * <p>This builds an intermediate tag per value, so it is a convenience for types that already
     * have a {@code Codec} rather than the preferred choice for large or frequent payloads.
     *
     * @param codec the codec to adapt
     * @param <T> the value type
     * @return a stream codec delegating to the given codec
     */
    public static <T> DLStreamCodec<T> fromCodec(Codec<T> codec) {
        Objects.requireNonNull(codec, "codec");
        return DLStreamCodec.of(
            (buf, value) -> {
                Tag tag = codec.encodeStart(NbtOps.INSTANCE, value)
                    .getOrThrow(false, error -> { throw new IllegalStateException("Could not encode value: " + error); });
                CompoundTag wrapper = new CompoundTag();
                wrapper.put(WRAPPED_VALUE_KEY, tag);
                buf.writeNbt(wrapper);
            },
            buf -> {
                CompoundTag wrapper = buf.readAnySizeNbt();
                if (wrapper == null) {
                    throw new IllegalStateException("Missing payload for codec-backed value.");
                }
                return codec.parse(NbtOps.INSTANCE, wrapper.get(WRAPPED_VALUE_KEY))
                    .getOrThrow(false, error -> { throw new IllegalStateException("Could not decode value: " + error); });
            }
        );
    }

    /**
     * Adapts a type that serializes itself to a compound tag.
     *
     * @param factory creates a blank instance before deserialization
     * @param <T> the value type
     * @return a stream codec delegating to the type's own NBT methods
     */
    public static <T extends INBTSerializable> DLStreamCodec<T> fromNbtSerializable(Supplier<T> factory) {
        Objects.requireNonNull(factory, "factory");
        return DLStreamCodec.of(
            (buf, value) -> buf.writeNbt(value.serializeNbt()),
            buf -> {
                T value = Objects.requireNonNull(factory.get(), "factory returned null");
                CompoundTag nbt = buf.readAnySizeNbt();
                value.deserializeNbt(nbt == null ? new CompoundTag() : nbt);
                return value;
            }
        );
    }
}
