/*
 * DragonLib 3 - 1.21.1
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class DLStaticRegistry<T extends IStaticRegisterable<T>> {
    private final LinkedHashMap<ResourceLocation, DLStaticRegistryObject<? extends T>> TYPES = new LinkedHashMap<>();

    public <S extends T> DLStaticRegistryObject<S> register(ResourceLocation id, Supplier<S> factory) {
        DLStaticRegistryObject<S> type = new DLStaticRegistryObject<>(id, Suppliers.memoize(factory));
        TYPES.put(id, type);
        return type;
    }

    public boolean has(ResourceLocation id) {
        return TYPES.containsKey(id);
    }

    @SuppressWarnings("unchecked")
    public DLStaticRegistryObject<T> get(ResourceLocation id) {
        return (DLStaticRegistryObject<T>)TYPES.get(id);
    }

    public T load(CompoundTag tag) {
        return DLStaticRegistryObject.load(tag, this::get);
    }

    public List<T> getAll() {
        List<T> result = new ArrayList<>(TYPES.size());
        for (DLStaticRegistryObject<? extends T> v : TYPES.values()) {
            result.add(v.get());
        }
        return result;
    }

}
