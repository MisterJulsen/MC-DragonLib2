package de.mrjulsen.mcdragonlib.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.nbt.CompoundTag;

/**
 * An internal, small clipboard system for storing various data types
 * in NBT format. Multiple different data types can be stored in parallel,
 * but only one instance per type.
 */
public class Clipboard {

    protected static final Map<Class<? extends INBTSerializable>, CompoundTag> clipboardData = new HashMap<>();

    /**
     * Stores the given data to the clipbaord. This will overwrite the last written data of this type.
     * @param <T> The data type
     * @param clipboardClass The class of the data type
     * @param data The data instance
     */
    public static <T extends INBTSerializable> void put(Class<T> clipboardClass, T data) {
        if (clipboardData.containsKey(clipboardClass)) {
            clipboardData.remove(clipboardClass);
        }

        clipboardData.put(clipboardClass, data.serializeNbt());
    }

    /**
     * Retrieves the data currently stored in the clipboard for the specified type, if any.
     * @param <T> The data type
     * @param clipboardClass The data type class
     * @return The data stored in the clipboard, if any.
     */
    public static <T extends INBTSerializable> Optional<T> get(Class<T> clipboardClass) {
        if (clipboardData.containsKey(clipboardClass)) {
            try {                
                T t = clipboardClass.getDeclaredConstructor().newInstance();
                t.deserializeNbt(clipboardData.get(clipboardClass));
                return Optional.of(t);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                DragonLib.LOGGER.error("Unable to deserialize clipbaord data.", e);
            }
        }
        return Optional.empty();
    }

    /**
     * @param <T> The data type
     * @param clipboardClass The data type class to check
     * @return {@code true} if data of this type has been stored in the clipboard, {@code false} otherwise.
     */
    public static <T extends INBTSerializable> boolean contains(Class<T> clipboardClass) {
        return clipboardData.containsKey(clipboardClass);
    }

    /**
     * Clears all stored data in the clipbaord.
     */
    public static void clear() {
        clipboardData.clear();
    }

    /**
     * Clears all stored data of the given type from the clipboard.
     * @param <T> The data type
     * @param clipboardClass The data type class to clear the data from
     * @return the data currently stored in the clipboard for the specified type, if any.
     */
    public static <T extends INBTSerializable> Optional<T> clear(Class<T> clipboardClass) {
        CompoundTag nbt = clipboardData.remove(clipboardClass);
        try {                
            T t = clipboardClass.getDeclaredConstructor().newInstance();
            t.deserializeNbt(nbt);
            return Optional.of(t);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            DragonLib.LOGGER.error("Unable to deserialize clipbaord data.", e);
        }
        return Optional.empty();
    }
}


