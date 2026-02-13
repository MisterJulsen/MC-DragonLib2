package de.mrjulsen.mcdragonlib.neoforge.client.model.loaders;

import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedQuad;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
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
