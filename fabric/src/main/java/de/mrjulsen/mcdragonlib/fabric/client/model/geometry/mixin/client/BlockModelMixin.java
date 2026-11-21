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

package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.mixin.client;

import java.util.List;
import java.util.function.Function;

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.VisibilityData;

import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.math.Transformation;

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.IUnbakedGeometry;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.extensions.BlockModelExtensions;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

@Mixin(BlockModel.class)
public class BlockModelMixin implements BlockModelExtensions {
	@Shadow
	@Final
	private List<ItemOverride> overrides;

	@Shadow
	@Nullable
    protected BlockModel parent;

	@Unique
	private IUnbakedGeometry<?> dragonlib$customModel;

	@Unique
	@Nullable
	private Transformation dragonlib$rootTransform;

	@Unique
	private final VisibilityData dragonlib$visibilityData = new VisibilityData();

	@Inject(
			method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;",
			at = @At("HEAD"),
			cancellable = true
	)
	public void dragonlib$handleCustomModels(ModelBaker modelBaker, BlockModel ownerModel, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, boolean guiLight3d, CallbackInfoReturnable<BakedModel> cir) {
		IUnbakedGeometry<?> geometry = dragonlib$getCustomGeometry();
		if (geometry != null) {
			ItemOverrides overrides = dragonlib$getOverrides(modelBaker, ownerModel, spriteGetter);
			cir.setReturnValue(geometry.bake((BlockModel) (Object) this, modelBaker, spriteGetter, modelTransform, overrides, guiLight3d));
		}
	}

	@Inject(method = "resolveParents", at = @At("HEAD"))
	private void dragonlib$handleCustomResolveParents(Function<ResourceLocation, UnbakedModel> function, CallbackInfo ci) {
		if (dragonlib$getCustomGeometry() != null)
			dragonlib$getCustomGeometry().resolveParents(function, (BlockModel) dragonlib$self());
	}

	@Override
	public ItemOverrides dragonlib$getOverrides(ModelBaker p_250138_, BlockModel p_251800_, Function<Material, TextureAtlasSprite> spriteGetter) {
		return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(p_250138_, p_251800_, this.overrides/*, spriteGetter*/);
	}

	@Override
	public void dragonlib$setCustomGeometry(IUnbakedGeometry<?> geometry) {
		this.dragonlib$customModel = geometry;
	}

	@Override
	public IUnbakedGeometry<?> dragonlib$getCustomGeometry() {
		return this.parent != null && dragonlib$customModel == null ? ((BlockModelExtensions)(Object)this.parent).dragonlib$getCustomGeometry() : dragonlib$customModel;
	}

	@Override
	public VisibilityData dragonlib$getVisibilityData() {
		return this.dragonlib$visibilityData;
	}

	@Override
	public boolean dragonlib$isComponentVisible(String part, boolean fallback) {
		return dragonlib$selfParent() != null && !dragonlib$visibilityData.hasCustomVisibility(part) ?
				dragonlib$selfParent().dragonlib$isComponentVisible(part, fallback) :
				dragonlib$visibilityData.isVisible(part, fallback);
	}

	@Override
	public Transformation dragonlib$getRootTransform() {
		if (dragonlib$rootTransform != null)
			return dragonlib$rootTransform;
		return dragonlib$selfParent() != null ? dragonlib$selfParent().dragonlib$getRootTransform() : Transformation.identity();
	}

	@Override
	public void dragonlib$setRootTransform(Transformation rootTransform) {
		this.dragonlib$rootTransform = rootTransform;
	}

	@Unique
	private BlockModelExtensions dragonlib$self() {
		return (BlockModelExtensions)(Object)this;
	}

	@Unique
	private BlockModelExtensions dragonlib$selfParent() {
		return (BlockModelExtensions)(Object)(((BlockModelAccessor)(Object) dragonlib$self()).dragonlib$getParent());
	}
}
