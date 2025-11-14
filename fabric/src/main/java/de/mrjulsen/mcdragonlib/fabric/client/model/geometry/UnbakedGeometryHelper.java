package de.mrjulsen.mcdragonlib.fabric.client.model.geometry;

import com.mojang.math.Transformation;

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.extensions.TransformationExtensions;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.mixin.client.BlockModelAccessor;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.mixin.client.ItemModelGeneratorAccessor;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnbakedGeometryHelper {
	private static final ItemModelGeneratorAccessor ITEM_MODEL_GENERATOR = (ItemModelGeneratorAccessor)new ItemModelGenerator();
	private static final FaceBakery FACE_BAKERY = new FaceBakery();

		private static final Pattern FILESYSTEM_PATH_TO_RESLOC =
			Pattern.compile("(?:.*[\\\\/]assets[\\\\/](?<namespace>[a-z_-]+)[\\\\/]textures[\\\\/])?(?<path>[a-z_\\\\/-]+)\\.png");

		@SuppressWarnings("deprecation")
	public static Material resolveDirtyMaterial(@Nullable String tex, @Nullable BlockModel owner) {
		if (tex == null)
			return new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
		if (tex.startsWith("#") && owner != null)
			return owner.getMaterial(tex);

		
		
		Matcher match = FILESYSTEM_PATH_TO_RESLOC.matcher(tex);
		if (match.matches()) {
			String namespace = match.group("namespace");
			String path = match.group("path").replace("\\", "/");
			tex = namespace != null ? namespace + ":" + path : path;
		}

		return new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(tex));
	}

		public static BakedQuad bakeElementFace(BlockElement element, BlockElementFace face, TextureAtlasSprite sprite, Direction direction, ModelState state, ResourceLocation modelLocation) {
		return FACE_BAKERY.bakeQuad(element.from, element.to, face, sprite, direction, state, element.rotation, element.shade, modelLocation);
	}

		public static RenderContext.QuadTransform applyRootTransform(ModelState modelState, Transformation rootTransform) {
		
		
		
		Transformation transform = ((TransformationExtensions)(Object)modelState.getRotation()).dragonlib$applyOrigin(new Vector3f(.5F, .5F, .5F));
		return QuadTransformers.applying(transform.compose(rootTransform).compose(transform.inverse()));
	}

	public static void bakeElements(List<BakedQuad> quads, List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation) {
		for (BlockElement element : elements) {
			element.faces.forEach((side, face) -> {
				@SuppressWarnings("deprecation")
				var sprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(face.texture)));
				quads.add(BlockModelAccessor.dragonlib$getFaceBakery().bakeQuad(element.from, element.to, face, sprite, side, modelState, element.rotation, element.shade, modelLocation));
			});
		}
	}

		public static List<BakedQuad> bakeElements(List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation) {
		if (elements.isEmpty())
			return List.of();
		var list = new ArrayList<BakedQuad>();
		bakeElements(list, elements, spriteGetter, modelState, modelLocation);
		return list;
	}

		public static List<BlockElement> createUnbakedItemElements(int layerIndex, SpriteContents spriteContents) {
		return ITEM_MODEL_GENERATOR.dragonlib$processFrames(layerIndex, "layer" + layerIndex, spriteContents);
	}

		public static List<BlockElement> createUnbakedItemMaskElements(int layerIndex, SpriteContents spriteContents) {
		var elements = createUnbakedItemElements(layerIndex, spriteContents);
		elements.remove(0); 

		int width = spriteContents.width(), height = spriteContents.height();
		var bits = new BitSet(width * height);

		
		spriteContents.getUniqueFrames().forEach(frame -> {
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
					if (!spriteContents.isTransparent(frame, x, y))
						bits.set(x + y * width);
		});

		
		for (int y = 0; y < height; y++) {
			int xStart = -1;
			for (int x = 0; x < width; x++) {
				var opaque = bits.get(x + y * width);
				if (opaque == (xStart == -1)) { 
					if (xStart == -1) {
						
						xStart = x;
						continue;
					}

					
					int yEnd = y + 1;
					expand:
					for (; yEnd < height; yEnd++)
						for (int x2 = xStart; x2 <= x; x2++)
							if (!bits.get(x2 + yEnd * width))
								break expand;

					
					for (int i = xStart; i < x; i++)
						for (int j = y; j < yEnd; j++)
							bits.clear(i + j * width);

					
					elements.add(new BlockElement(
							new Vector3f(16 * xStart / (float) width, 16 - 16 * yEnd / (float) height, 7.5F),
							new Vector3f(16 * x / (float) width, 16 - 16 * y / (float) height, 8.5F),
							Util.make(new HashMap<>(), map -> {
								for (Direction direction : Direction.values())
									map.put(direction, new BlockElementFace(null, layerIndex, "layer" + layerIndex, new BlockFaceUV(null, 0)));
							}),
							null,
							true
					));

					
					xStart = -1;
				}
			}
		}
		return elements;
	}

		public static ModelState composeRootTransformIntoModelState(ModelState modelState, Transformation rootTransform) {
		
		
		rootTransform = ((TransformationExtensions)(Object)rootTransform).dragonlib$applyOrigin(new Vector3f(-.5F, -.5F, -.5F));
		return new SimpleModelState(modelState.getRotation().compose(rootTransform), modelState.isUvLocked());
	}
}
