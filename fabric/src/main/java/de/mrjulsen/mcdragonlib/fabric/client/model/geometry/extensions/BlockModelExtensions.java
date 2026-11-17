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

package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.extensions;

import com.mojang.math.Transformation;

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.IUnbakedGeometry;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.VisibilityData;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;

import java.util.function.Function;

public interface BlockModelExtensions {

	default ItemOverrides dragonlib$getOverrides(ModelBaker pModelBakery, BlockModel pModel, Function<Material, TextureAtlasSprite> textureGetter) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default void dragonlib$setCustomGeometry(IUnbakedGeometry<?> geometry) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default IUnbakedGeometry<?> dragonlib$getCustomGeometry() {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default boolean dragonlib$isComponentVisible(String part, boolean fallback) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default VisibilityData dragonlib$getVisibilityData() {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default Transformation dragonlib$getRootTransform() {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default void dragonlib$setRootTransform(Transformation rootTransform) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}
}
