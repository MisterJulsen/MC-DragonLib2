package de.mrjulsen.mcdragonlib.data;

import net.minecraft.nbt.CompoundTag;

public interface INBTSerializable {
    CompoundTag serializeNbt();
    void deserializeNbt(CompoundTag nbt);
}
