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

package de.mrjulsen.mcdragonlib.client.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.LinkedList;
import java.util.List;

public abstract class ModelResourceLocationBuilder {

    private final ResourceLocation location;

    public ModelResourceLocationBuilder(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public abstract ModelResourceLocation build();


    public static class Base extends ModelResourceLocationBuilder {

        public Base(ResourceLocation location) {
            super(location);
        }

        @Override
        public ModelResourceLocation build() {
            return new ModelResourceLocation(getLocation(), "");
        }
    }

    public static class BlockState extends ModelResourceLocationBuilder {

        private final List<String> properties = new LinkedList<>();

        public BlockState(ResourceLocation location) {
            super(location);
        }

        public <T extends Comparable<T>> BlockState with(Property<T> property, T value) {
            this.properties.add(property.getName() + "=" + property.getName(value));
            return this;
        }

        @Override
        public ModelResourceLocation build() {
            return new ModelResourceLocation(getLocation(), String.join(",", this.properties));
        }
    }

    public static class Inventory extends ModelResourceLocationBuilder {

        public Inventory(ResourceLocation location) {
            super(location);
        }

        @Override
        public ModelResourceLocation build() {
            return new ModelResourceLocation(getLocation(), "inventory");
        }
    }
}
