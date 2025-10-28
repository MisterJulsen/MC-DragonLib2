package de.mrjulsen.mcdragonlib.client.model;

import java.util.Collection;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Register custom {@link BakedModel}s for {@link Block}s and their {@link BlockItem}s. 
 */
public class CustomBlockModelRegistry implements ResourceManagerReloadListener {

    private static final Map<BlockState, BakedModel> originalModels = new ConcurrentHashMap<>();

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

    public static Queue<ICustomModelFactory> getCustomRegisteredModels(Map<ResourceLocation, BakedModel> registry) {
        return new LinkedBlockingQueue<>(customModels);
    }

    public static ImmutableMap<ResourceLocation, ModelRegistryData> getCustomRegisteredModelsMapped() {
        ImmutableMap.Builder<ResourceLocation, ModelRegistryData> builder = ImmutableMap.builder();
        while (!customModels.isEmpty()) {
            ICustomModelFactory factory = customModels.poll();
            if (factory.getModelFactory() != null) {
                for (BlockState state : factory.getStates()) {
                    ResourceLocation location = BlockModelShaper.stateToModelLocation(state);
                    if (factory.getType() == ModelType.ITEM) {
                        location = new ModelResourceLocation(location, "inventory");
                    }
                    builder.put(location, new ModelRegistryData(factory, state));
                }
            }
        }
        return builder.build();
    }


    @Internal
    public static void setOriginalModel(BlockState state, BakedModel model) {
        originalModels.computeIfAbsent(state, s -> model);
    }

    public static BakedModel getOriginalModel(BlockState state) {
        if (originalModels.containsKey(state)) {
            return originalModels.get(state);
        }
        return Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }
}
