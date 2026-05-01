package de.mrjulsen.mcdragonlib.fabric.client.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
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
	private static final RenderMaterial MATERIAL_STANDARD = RENDERER.materialFinder().find();
	private static final RenderMaterial MATERIAL_NO_AO = RENDERER.materialFinder().ambientOcclusion(TriState.FALSE).find();

    private final BlockState defaultState;
    private final BakedModel src;
    private final DLModel newModel;

    public DynamicBakedModel(BakedModel src, BlockState defaultState, DLModel newModel) {
        Objects.requireNonNull(src);
        Objects.requireNonNull(defaultState);
        Objects.requireNonNull(newModel);

        this.src = src;
        this.defaultState = defaultState;
        this.newModel = newModel;
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

    private void emitQuads(RenderContext context, RandomSource rand, BlockState state, ModelType type,ModelContext modelContext) {
		final MaterialFinder materialFinder = useAmbientOcclusion() ? RENDERER.materialFinder() : RENDERER.materialFinder().ambientOcclusion(TriState.FALSE);
        Map<RenderType, RenderMaterial> materialByRenderType = new HashMap<>();
        
        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
			final Direction cullFace = ModelHelper.faceFromIndex(i);

			if (!context.hasTransform() && (type == ModelType.BLOCK && context.isFaceCulled(cullFace))) {
				continue;
			}

            for (RenderType renderType : newModel.getSupportedRenderTypes()) {
                final List<BakedQuad> quads = newModel.getQuads(type, src, state, rand, renderType, cullFace, modelContext);
                final int count = quads.size();

                for (int j = 0; j < count; j++) {
                    final BakedQuad q = quads.get(j);
                    context.getEmitter()
                        .fromVanilla(q, materialByRenderType.computeIfAbsent(renderType, x -> materialFinder.blendMode(BlendMode.fromRenderLayer(renderType)).find()), cullFace)
                        .emit();
                }
            }
		}
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
