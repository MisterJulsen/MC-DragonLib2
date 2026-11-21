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

package de.mrjulsen.mcdragonlib.client.model.extension.neoforge;

import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.QuadTransformers;

import java.util.List;

public class DLBakedQuadImpl extends DLBakedQuad {

    protected DLBakedQuadImpl(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        super(vertices, tintIndex, direction, sprite, shade, ambientOcclusion, emissive, tags);
    }

    @Override
    public boolean hasAmbientOcclusion() {
        return isAmbientOcclusion();
    }

    public static DLBakedQuad create(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        BakedQuad quad = new BakedQuad(vertices, tintIndex, direction, sprite, shade, ambientOcclusion);
        if (emissive) {
            QuadTransformers.settingMaxEmissivity().processInPlace(quad);
        }

        return new DLBakedQuadImpl(
                quad.getVertices(),
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                !emissive && quad.isShade(),
                !emissive && ambientOcclusion,
                emissive,
                tags
        );
    }
}
