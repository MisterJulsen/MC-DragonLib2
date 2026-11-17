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

package de.mrjulsen.mcdragonlib.forge.client.model.loaders;

import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Map;
import java.util.function.Function;

public class DLUnbakedModelExtension implements IUnbakedGeometry<DLUnbakedModelExtension> {

    private final BlockModel parent;
    private final Map<DLFaceKey, DLFaceData> faceData;

    public DLUnbakedModelExtension(BlockModel parent, Map<DLFaceKey, DLFaceData> data) {
        this.parent = parent;
        this.faceData = data;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext ctx, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel vanilla = parent.bake(baker, parent, spriteGetter, modelState, modelLocation, ctx.isGui3d());
        return new DLBakedModelExtension(vanilla, DLFaceKey.rotate(faceData, modelState));
    }
}
