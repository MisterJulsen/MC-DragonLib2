package de.mrjulsen.mcdragonlib.events.client;

import java.util.function.Predicate;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ModelEvents {

    /**
     * Registers models that the game would not load on its own because nothing references them from a block state
     * or item model file. They are baked like any other model and can be fetched through
     * {@code ModelUtils.getModel(ModelResourceLocation)}.
     */
    Event<AdditionalModels> ADDITIONAL_MODELS = EventFactory.createLoop();

    /**
     * Replaces baked top-level models, i.e. block state variants and {@code inventory} item models. Models that
     * are only loaded as part of another model are not covered by this event.
     * <p>
     * Listeners provide the models they want to replace, they are not handed every model in the game:
     *
     * <pre>{@code
     * ModelEvents.MODIFY_MODELS.register(models ->
     *      models.replaceBlock(MY_BLOCK.get(), (original, context) -> new MyWrapper(original)));
     * }</pre>
     */
    Event<ModifyModels> MODIFY_MODELS = EventFactory.createLoop();



    @FunctionalInterface
    interface AdditionalModels {
        void registerAdditionalModels(AdditionalModelsRegistry registry);

        @FunctionalInterface
        interface AdditionalModelsRegistry {
            void register(ResourceLocation model);
        }
    }

    @FunctionalInterface
    interface ModifyModels {
        void collectModels(ModelModifier modifier);

        interface ModelModifier {
            void replace(ResourceLocation location, ModelReplacer replacer);
            void replaceIf(Predicate<ResourceLocation> filter, ModelReplacer replacer);

            default void replaceBlock(Block block, ModelReplacer replacer) {
                for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                    replace(BlockModelShaper.stateToModelLocation(state), replacer);
                }
            }

            default void replaceState(BlockState state, ModelReplacer replacer) {
                replace(BlockModelShaper.stateToModelLocation(state), replacer);
            }

            default void replaceItem(Item item, ModelReplacer replacer) {
                replace(new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(item), "inventory"), replacer);
            }

            default void replaceNamespace(String namespace, ModelReplacer replacer) {
                for (Block block : BuiltInRegistries.BLOCK) {
                    if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(namespace)) {
                        replaceBlock(block, replacer);
                    }
                }
                for (Item item : BuiltInRegistries.ITEM) {
                    if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(namespace)) {
                        replaceItem(item, replacer);
                    }
                }
            }
        }

        @FunctionalInterface
        interface ModelReplacer {
            BakedModel replace(@Nullable BakedModel originalModel, Context context);
        }

        interface Context {
            ModelBakery getModelBakery();
            ResourceLocation getModelLocation();
            UnbakedModel getUnbakedModel();
        }
    }
}
