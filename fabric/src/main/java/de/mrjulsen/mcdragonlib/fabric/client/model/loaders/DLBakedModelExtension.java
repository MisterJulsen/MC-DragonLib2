package de.mrjulsen.mcdragonlib.fabric.client.model.loaders;

import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedQuad;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DLBakedModelExtension extends ForwardingBakedModel {

    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();
    private static final RenderMaterial DEFAULT_MAT = RENDERER.materialFinder()
            .find();

    private static final RenderMaterial EMISSIVE_MAT = RENDERER.materialFinder()
            .emissive(true)
            .ambientOcclusion(TriState.FALSE)
            .disableDiffuse(true)
            .find();

    private static final RenderMaterial NO_AO_MAT = RENDERER.materialFinder()
            .ambientOcclusion(TriState.FALSE)
            .find();


    private final Map<DLFaceKey, DLFaceData> faceData;


    private record CacheKey(BlockState state, Direction side) {}
    private final Map<CacheKey, List<BakedQuad>> cachedQuads = new ConcurrentHashMap<>();

    public DLBakedModelExtension(BakedModel parent, Map<DLFaceKey, DLFaceData> faceData) {
        this.wrapped = parent;
        this.faceData = faceData;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        emitQuads(context, randomSupplier.get(), state);
    }

    private void emitQuads(RenderContext context, RandomSource rand, BlockState state) {
        QuadEmitter emitter = context.getEmitter();

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            final Direction cullFace = ModelHelper.faceFromIndex(i);

            if (cullFace != null && context.isFaceCulled(cullFace)) {
                continue;
            }

            List<BakedQuad> quads = getQuads(state, cullFace, rand);
            for (BakedQuad q : quads) {
                if (q instanceof DLBakedQuad dlq) {
                    context.pushTransform(quadView -> {
                        RenderMaterial mat = DEFAULT_MAT;
                        if (dlq.isEmissive()) {
                            mat = EMISSIVE_MAT;
                        } else if (!dlq.isAmbientOcclusion()) {
                            mat = NO_AO_MAT;
                        }
                        quadView.fromVanilla(dlq, mat, cullFace);
                        return true;
                    });
                    emitter.emit();
                    context.popTransform();
                } else {
                    emitter.fromVanilla(q, DEFAULT_MAT, cullFace);
                    emitter.emit();
                }
            }
        }
    }

    public static MaterialFinder applyMaterial(DLBakedQuad quad, MaterialFinder material) {
        if (quad.isEmissive()) {
            return material.emissive(true).ambientOcclusion(TriState.FALSE).disableDiffuse(true);
        } else if (!quad.isAmbientOcclusion()) {
            return material.copyFrom(NO_AO_MAT);
        }
        return material;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState blockState, Direction face, RandomSource rand) {
        return cachedQuads.computeIfAbsent(new CacheKey(blockState, face), k -> {
            List<BakedQuad> src = super.getQuads(blockState, face, rand);
            List<BakedQuad> quads = new ArrayList<>(src.size());
            int i = 0;
            for (BakedQuad q : src) {
                DLFaceKey key = new DLFaceKey(i, face);
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
