package de.mrjulsen.mcdragonlib.forge.client.model;

import java.util.List;
import java.util.Objects;

import de.mrjulsen.mcdragonlib.client.model.extension.forge.DLBakedModelWrapperImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.client.model.ICustomModelBlockEntity;
import de.mrjulsen.mcdragonlib.client.model.IDynamicBakedModel;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel.ModelType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class DynamicBakedModel extends DLBakedModelWrapperImpl implements IDynamicBakedModel {

    private final ModelProperty<ModelContext> MODEL_CONTEXT_PROPERTY = new ModelProperty<>();

    private final BlockState defaultState;
    private final BakedModel src;
    private final DLModel newModel;

    public DynamicBakedModel(BakedModel src, BlockState defaultState, DLModel newModel) {
        super(src);
        Objects.requireNonNull(defaultState);
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
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {        
        ModelType type = ModelType.isItem(state == null);
        return newModel.getQuads(type, src, state == null ? defaultState : state, rand, renderType, side, data.has(MODEL_CONTEXT_PROPERTY) ? data.get(MODEL_CONTEXT_PROPERTY) : ModelContext.EMPTY);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        if (level.getBlockEntity(pos) instanceof ICustomModelBlockEntity be) {
            return ModelData.builder().with(MODEL_CONTEXT_PROPERTY, be.getModelContext()).build();
        }
        return modelData;
    }
   
    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(newModel.getSupportedRenderTypes());
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return newModel.useAmbientOcclusion() == null ? src.useAmbientOcclusion() : newModel.useAmbientOcclusion();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction direction, @NotNull RandomSource rand) {
        return getQuads(state, direction, rand, ModelData.EMPTY, null);
    }
    
}
