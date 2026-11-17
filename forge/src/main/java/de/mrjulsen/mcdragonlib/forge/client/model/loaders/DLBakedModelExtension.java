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

import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedQuad;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DLBakedModelExtension extends BakedModelWrapper<BakedModel> {

    private final Map<DLFaceKey, DLFaceData> faceData;

    private record CacheKey(BlockState state, Direction side) {}
    private final Map<CacheKey, List<BakedQuad>> cachedQuads = new ConcurrentHashMap<>();

    public DLBakedModelExtension(BakedModel parent, Map<DLFaceKey, DLFaceData> faceData) {
        super(parent);
        this.faceData = faceData;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        return getQuads(state, side, rand);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
        return cachedQuads.computeIfAbsent(new CacheKey(state, side), k -> {
            List<BakedQuad> src = super.getQuads(state, side, rand);
            List<BakedQuad> quads = new ArrayList<>(src.size());
            int i = 0;
            for (BakedQuad q : src) {
                DLFaceKey key = new DLFaceKey(i, side);
                DLFaceData data = faceData.get(key);
                if (data != null) {
                    quads.add(DLBakedQuad.create(q, data));
                } else {
                    quads.add(q);
                }
                i++;
            }
            return quads;
        });
    }


}
