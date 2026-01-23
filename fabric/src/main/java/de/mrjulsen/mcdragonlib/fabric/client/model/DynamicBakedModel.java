package de.mrjulsen.mcdragonlib.fabric.client.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.client.model.ICustomModelBlockEntity;
import de.mrjulsen.mcdragonlib.client.model.IDynamicBakedModel;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedQuad;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.fabric.DLBakedQuadImpl;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel.ModelType;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.DLBakedModelExtension;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class DynamicBakedModel extends ForwardingBakedModel implements IDynamicBakedModel {
    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();

    private final BlockState defaultState;
    private final DLModel newModel;

    private record MaterialKey(RenderType renderType, boolean ambientOcclusion, boolean emissive) {
        public static MaterialKey create(RenderType rendertype, BakedQuad bakedquad) {
            if (bakedquad instanceof DLBakedQuad ext) {
                return new MaterialKey(rendertype, ext.isAmbientOcclusion(), ext.isEmissive());
            }
            return new MaterialKey(rendertype, DLFaceData.DEFAULT.ambientOcclusion(), DLFaceData.DEFAULT.emissive());
        }
    }
    private final Map<MaterialKey, RenderMaterial> materialCache = new ConcurrentHashMap<>();

    public DynamicBakedModel(BakedModel src, BlockState defaultState, DLModel newModel) {
        this.wrapped = Objects.requireNonNull(src);
        this.defaultState = Objects.requireNonNull(defaultState);
        this.newModel = Objects.requireNonNull(newModel);
    }

    private RenderMaterial getMaterial(RenderType renderType, BakedQuad quad) {
        return materialCache.computeIfAbsent(MaterialKey.create(renderType, quad), key -> {
            MaterialFinder finder = RENDERER.materialFinder().blendMode(BlendMode.fromRenderLayer(key.renderType));
            if (quad instanceof DLBakedQuad ext) {
                finder = DLBakedModelExtension.applyMaterial(ext, finder);
            }
            return finder.find();
        });
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        ModelContext modelContext = ModelContext.EMPTY;
        if (blockView.getBlockEntity(pos) instanceof ICustomModelBlockEntity be) {
            modelContext = be.getModelContext();
        }
        emitQuads(context, randomSupplier.get(), state, ModelType.BLOCK, modelContext);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        emitQuads(context, randomSupplier.get(), defaultState, ModelType.ITEM, ModelContext.EMPTY);
    }

    private void emitQuads(RenderContext context, RandomSource rand, BlockState state, ModelType type, ModelContext modelContext) {
        QuadEmitter emitter = context.getEmitter();

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            final Direction cullFace = ModelHelper.faceFromIndex(i);

            if (type == ModelType.BLOCK && cullFace != null && context.isFaceCulled(cullFace)) {
                continue;
            }

            for (RenderType renderType : newModel.getSupportedRenderTypes()) {
                final List<BakedQuad> quads = newModel.getQuads(type, wrapped, state, rand, renderType, cullFace, modelContext);

                for (BakedQuad q : quads) {
                    emitter.fromVanilla(q, getMaterial(renderType, q), cullFace).emit();
                }
            }
        }
    }



    @Override
    public BakedModel getOriginalModel() {
        return wrapped;
    }

    @Override
    public DLModel getModel() {
        return newModel;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }
}
