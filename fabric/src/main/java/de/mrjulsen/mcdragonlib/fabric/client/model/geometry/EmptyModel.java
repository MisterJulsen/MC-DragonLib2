package de.mrjulsen.mcdragonlib.fabric.client.model.geometry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public class EmptyModel implements IUnbakedGeometry<EmptyModel> {
	public static final BakedModel BAKED = new Baked();
	public static final EmptyModel INSTANCE = new EmptyModel();
	public static final IGeometryLoader<EmptyModel> LOADER = (json, ctx) -> INSTANCE;

	private EmptyModel() {}

	@Override
	public BakedModel bake(BlockModel context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, boolean isGui3d) {
		return BAKED;
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, BlockModel context) {
		
	}

	private static class Baked extends SimpleBakedModel {
		@SuppressWarnings("deprecation")
		private static final Material MISSING_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());

		public Baked() {
			super(List.of(), Map.of(), false, false, false, MISSING_TEXTURE.sprite(), ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY);
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			return MISSING_TEXTURE.sprite();
		}
	}
}
