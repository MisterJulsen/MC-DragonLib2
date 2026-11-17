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

package de.mrjulsen.mcdragonlib.util.registry;

import java.util.function.Function;

import com.google.common.base.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final record DLStaticRegistryObject<T>(ResourceLocation id, Supplier<T> factory) {

    public static final String NBT_ID = "Id";

    public CompoundTag wrap(T data) {
        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_ID, id.toString());
        return tag;
    }
    
    public static <T> T load(CompoundTag tag, Function<ResourceLocation, DLStaticRegistryObject<T>> registryGetter) {
        ResourceLocation typeId = new ResourceLocation(tag.getString(NBT_ID));
        DLStaticRegistryObject<T> type = registryGetter.apply(typeId);
        if (type == null) return null;
        return type.get();
    }

    public T get() {
        return factory.get();
    }
}

