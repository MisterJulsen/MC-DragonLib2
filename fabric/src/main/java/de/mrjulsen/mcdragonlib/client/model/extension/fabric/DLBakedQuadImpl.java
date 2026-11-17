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

package de.mrjulsen.mcdragonlib.client.model.extension.fabric;

import de.mrjulsen.mcdragonlib.client.model.ModelUtils;
import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedQuad;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.List;

public class DLBakedQuadImpl extends DLBakedQuad {

    protected DLBakedQuadImpl(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        super(vertices, tintIndex, direction, sprite, shade, ambientOcclusion, emissive, tags);
    }

    public static DLBakedQuad create(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        if (emissive) {
            int corners = 4;
            int[] lightmap = new int[2];
            for (int i = 0; i < corners; i++) {
                ModelUtils.unpackLight(vertices, lightmap, i);
                Arrays.fill(lightmap, LightTexture.FULL_BRIGHT);
                ModelUtils.packLight(lightmap, vertices, i);
            }
        }

        return new DLBakedQuadImpl(
                vertices,
                tintIndex,
                direction,
                sprite,
                !emissive && shade,
                !emissive && ambientOcclusion,
                emissive,
                tags
        );
    }
}
