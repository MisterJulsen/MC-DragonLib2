package de.mrjulsen.mcdragonlib.fabric.client.model.geometry;

import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>> {
	BakedModel bake(BlockModel context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, boolean isGui3d);

	default void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, BlockModel context) {

	}

	default Set<String> getConfigurableComponentNames() {
		return Set.of();
	}
}
