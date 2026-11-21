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

package de.mrjulsen.mcdragonlib.client.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.google.common.collect.ImmutableMap;

import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel.ModelType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Register custom {@link BakedModel}s for {@link Block}s and their {@link BlockItem}s. 
 */
public class DLBlockModelRegistry implements ResourceManagerReloadListener {

    private static final Map<BlockState, BakedModel> originalModels = new ConcurrentHashMap<>();
    private static final Map<BlockState, BakedModel> originalItemModels = new ConcurrentHashMap<>();

    private static final Queue<ICustomModelFactory> customModels = new LinkedBlockingQueue<>();

    public static interface ICustomModelFactory {
        Collection<BlockState> getStates();
        Supplier<DLModel> getModelFactory();
        ModelType getType();
    }

    private static abstract class AbstractModelFactory implements ICustomModelFactory {
        private final Supplier<DLModel> modelFactory;

        public AbstractModelFactory(Supplier<DLModel> modelFactory) {
            this.modelFactory = modelFactory;
        }

        @Override
        public Supplier<DLModel> getModelFactory() {
            return modelFactory;
        }
    }

    private static class BlockModelFactory extends AbstractModelFactory {

        private final Supplier<Block> block;

        public BlockModelFactory(Supplier<Block> block, Supplier<DLModel> modelFactory) {
            super(modelFactory);
            this.block = block;
        }

        @Override
        public Collection<BlockState> getStates() {
            return block.get().getStateDefinition().getPossibleStates();
        }

        @Override
        public ModelType getType() {
            return ModelType.BLOCK;
        }
    }

    private static class BlockStateModelFactory extends AbstractModelFactory {

        private final Supplier<Collection<BlockState>> blockStates;

        public BlockStateModelFactory(Supplier<Collection<BlockState>> blockStates, Supplier<DLModel> modelFactory) {
            super(modelFactory);
            this.blockStates = blockStates;
        }

        @Override
        public Collection<BlockState> getStates() {
            return blockStates.get();
        }

        @Override
        public ModelType getType() {
            return ModelType.BLOCK;
        }
    }

    private static class BlockItemModelFactory extends AbstractModelFactory {

        private final Supplier<BlockState> itemBlockState;

        public BlockItemModelFactory(Supplier<BlockState> itemBlockState, Supplier<DLModel> modelFactory) {
            super(modelFactory);
            this.itemBlockState = itemBlockState;
        }

        @Override
        public Collection<BlockState> getStates() {
            return List.of(itemBlockState.get());
        }

        @Override
        public ModelType getType() {
            return ModelType.ITEM;
        }
    }

    /**
     * Register a new model for the specified block and swap it accordingly for all possible {@link BlockState} from {@link StateDefinition#getPossibleStates()} when loading the game .
     * @param block The block of which the models are to be replaced.
     * @param blockModelFactory The new model for the {@link Block} or {@code null} if the original model should be used.
     * @param itemModelFactory The new model for the {@link BlockItem} or {@code null} if the original model should be used.
     * For items the default {@link BlockState} from {@link Block#defaultBlockState()} will be used.
     */
    public static void registerForBlock(Supplier<Block> block, Supplier<DLModel> blockModelFactory, Supplier<DLModel> itemModelFactory) {
        if (blockModelFactory != null) customModels.add(new BlockModelFactory(block, blockModelFactory));
        if (itemModelFactory != null) customModels.add(new BlockItemModelFactory(() -> block.get().defaultBlockState(), itemModelFactory));
    }
    
    /**
     * Register a new model for the specified {@link BlockState} and swap it accordingly when loading the game.
     * @param blockStates The block states for which the models should be replaced.
     * @param modelFactory The new model for the given states or {@code null} if the original model should be used.
     */
    public static void registerForStates(Supplier<Collection<BlockState>> blockStates, Supplier<DLModel> modelFactory) {
        customModels.add(new BlockStateModelFactory(blockStates, modelFactory));
    }
    
    /**
     * Register a new model for the {@link BlockItem} of the {@link Block} specified by a {@link BlockState} and swap it accordingly when loading the game.
     * @param itemBlockState The {@link BlockState} which should be used as a reference when building the item model.
     * @param modelFactory The new model for the {@link BlockItem} or {@code null} if the original model should be used.
     */
    public static void registerForBlockItem(Supplier<BlockState> itemBlockState, Supplier<DLModel> modelFactory) {
        customModels.add(new BlockItemModelFactory(itemBlockState, modelFactory));
    }


    public static record ModelRegistryData(ICustomModelFactory factory, BlockState state) {
        @Override
        public final int hashCode() {
            return factory.hashCode();
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj instanceof ModelRegistryData o) {
                return factory == o.factory;
            }
            return false;
        }
    }

    public static Queue<ICustomModelFactory> getCustomRegisteredModels(Map<ModelResourceLocation, BakedModel> registry) {
        return new LinkedBlockingQueue<>(customModels);
    }

    /**
     * Maps every registered model to the location of the baked model it replaces.
     * <p>
     * Keyed by the full {@link ModelResourceLocation} including its variant, because all block states of a block
     * share the same {@link ModelResourceLocation#id()}. The registrations are kept, so this may be called once
     * per resource reload.
     */
    public static ImmutableMap<ModelResourceLocation, ModelRegistryData> getCustomRegisteredModelsMapped() {
        Map<ModelResourceLocation, ModelRegistryData> mapped = new LinkedHashMap<>();
        for (ICustomModelFactory factory : customModels) {
            if (factory.getModelFactory() != null) {
                for (BlockState state : factory.getStates()) {
                    ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
                    if (factory.getType() == ModelType.ITEM) {
                        location = new ModelResourceLocation(location.id(), "inventory");
                    }
                    mapped.put(location, new ModelRegistryData(factory, state));
                }
            }
        }
        return ImmutableMap.copyOf(mapped);
    }


    /**
     * Same as {@link #setOriginalModel(ModelType, BlockState, BakedModel)} with {@link ModelType#BLOCK}.
     */
    public static void setOriginalModel(BlockState state, BakedModel model) {
        setOriginalModel(ModelType.BLOCK, state, model);
    }

    /**
     * Stores the original model that has been replaced by a dynamic model. Called on every model bake, so the
     * stored model is always the one of the latest resource reload. DragonLib's own dynamic models are never
     * stored as original model.
     */
    public static void setOriginalModel(ModelType type, BlockState state, BakedModel model) {
        if (state == null || model instanceof IDynamicBakedModel) {
            return;
        }
        Map<BlockState, BakedModel> models = type == ModelType.ITEM ? originalItemModels : originalModels;
        if (model == null) {
            models.remove(state);
        } else {
            models.put(state, model);
        }
    }

    /**
     * Same as {@link #getOriginalModel(ModelType, BlockState)} with {@link ModelType#BLOCK}.
     */
    public static BakedModel getOriginalModel(BlockState state) {
        return getOriginalModel(ModelType.BLOCK, state);
    }

    public static BakedModel getOriginalModel(ModelType type, BlockState state) {
        if (state == null) {
            return null;
        }
        BakedModel model = (type == ModelType.ITEM ? originalItemModels : originalModels).get(state);
        if (model != null) {
            return model;
        }
        return Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }
}
