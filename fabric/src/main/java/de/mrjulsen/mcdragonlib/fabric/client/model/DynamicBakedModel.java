package de.mrjulsen.mcdragonlib.fabric.client.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.client.model.ICustomModelBlockEntity;
import de.mrjulsen.mcdragonlib.client.model.IDynamicBakedModel;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel.ModelType;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class DynamicBakedModel implements BakedModel, IDynamicBakedModel {
    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();

    private final BlockState defaultState;
    private final BakedModel src;
    private final DLModel newModel;

    private record MaterialKey(RenderType renderType) {}
    private final Map<MaterialKey, RenderMaterial> materialCache = new ConcurrentHashMap<>();

    public DynamicBakedModel(BakedModel src, BlockState defaultState, DLModel newModel) {
        this.src = Objects.requireNonNull(src);
        this.defaultState = Objects.requireNonNull(defaultState);
        this.newModel = Objects.requireNonNull(newModel);
    }

    private RenderMaterial getMaterial(RenderType renderType, BakedQuad quad) {
        return materialCache.computeIfAbsent(new MaterialKey(renderType), key -> {
            MaterialFinder finder = RENDERER.materialFinder()
                    .blendMode(BlendMode.fromRenderLayer(key.renderType));
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
                final List<BakedQuad> quads = newModel.getQuads(type, src, state, rand, renderType, cullFace, modelContext);

                for (BakedQuad q : quads) {
                    RenderMaterial material = getMaterial(renderType, q);
                    emitter.fromVanilla(q, material, cullFace);
                    emitter.emit();
                }
            }
        }
    }



    @Override
    public BakedModel getOriginalModel() {
        return src;
    }

    @Override
    public DLModel getModel() {
        return newModel;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState states, Direction side, RandomSource rand) {        
        return List.of();
    }

    @Override
    public ItemOverrides getOverrides() {
        return src.getOverrides();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return src.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return src.getTransforms();
    }

    @Override
    public boolean isCustomRenderer() {
        return src.isCustomRenderer();
    }

    @Override
    public boolean isGui3d() {
        return src.isGui3d();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return newModel.useAmbientOcclusion() == null ? src.useAmbientOcclusion() : newModel.useAmbientOcclusion();
    }

    @Override
    public boolean usesBlockLight() {
        return src.usesBlockLight();
    }
    
}
