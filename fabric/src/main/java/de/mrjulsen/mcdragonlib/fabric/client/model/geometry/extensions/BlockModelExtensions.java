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
