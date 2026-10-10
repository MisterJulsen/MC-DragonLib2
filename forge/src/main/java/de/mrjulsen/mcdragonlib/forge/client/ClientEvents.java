package de.mrjulsen.mcdragonlib.forge.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry.ICustomModelFactory;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel.ModelType;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents.ModifyModels;
import de.mrjulsen.mcdragonlib.events.client.ModelReplacements;
import de.mrjulsen.mcdragonlib.forge.client.model.DynamicBakedModel;
import de.mrjulsen.mcdragonlib.forge.client.model.loaders.DLModelExtensionLoader;
import de.mrjulsen.mcdragonlib.forge.client.model.loaders.MultipartObjLoader;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    private static final class MutableContext implements ModifyModels.Context {

        private final ModelEvent.ModifyBakingResult event;
        private ResourceLocation location;

        private MutableContext(ModelEvent.ModifyBakingResult event) {
            this.event = event;
        }

        @Override
        public ModelBakery getModelBakery() {
            return event.getModelBakery();
        }

        @Override
        public ResourceLocation getModelLocation() {
            return location;
        }

        @Override
        public UnbakedModel getUnbakedModel() {
            return event.getModelBakery().getModel(location);
        }
    }


    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("multipart_obj", MultipartObjLoader.INSTANCE);
        event.register("advanced_json", DLModelExtensionLoader.INSTANCE);
    }


    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        ModelEvents.ADDITIONAL_MODELS.invoker().registerAdditionalModels(event::register);
    }


    @SubscribeEvent
    public static void onModifyBakingResult(final ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> registry = event.getModels();
        applyModelReplacements(event, registry);
        replaceRegisteredModels(registry);
    }

    private static void applyModelReplacements(final ModelEvent.ModifyBakingResult event, Map<ResourceLocation, BakedModel> registry) {
        ModelReplacements replacements = ModelReplacements.collect();
        if (replacements.isEmpty()) {
            return;
        }

        MutableContext context = new MutableContext(event);
        Function<ResourceLocation, BakedModel> lookup = registry::get;
        Map<ResourceLocation, BakedModel> modified = new HashMap<>();

        if (replacements.hasFilters()) {
            for (ResourceLocation location : registry.keySet()) {
                context.location = location;
                BakedModel model = replacements.apply(location, lookup, context);
                if (model != null) {
                    modified.put(location, model);
                }
            }
        } else {
            for (ResourceLocation location : replacements.namedLocations()) {
                context.location = location;
                BakedModel model = replacements.apply(location, lookup, context);
                if (model != null) {
                    modified.put(location, model);
                }
            }
        }

        if (!modified.isEmpty()) {
            registry.putAll(modified);
        }
    }


    private static void replaceRegisteredModels(Map<ResourceLocation, BakedModel> registry) {
        Queue<ICustomModelFactory> replacements = DLBlockModelRegistry.getCustomRegisteredModels(registry);
        while (!replacements.isEmpty()) {
            ICustomModelFactory r = replacements.poll();

            if (r.getModelFactory() != null && !r.getStates().isEmpty()) {
                for (BlockState state : r.getStates()) {
                    ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
                    if (r.getType() == ModelType.ITEM) {
                        location = new ModelResourceLocation(location, "inventory");
                    }
                    BakedModel originalModel = registry.get(location);
                    DLBlockModelRegistry.setOriginalModel(state, originalModel);
                    DynamicBakedModel newModel = new DynamicBakedModel(originalModel, state, r.getModelFactory().get());
                    registry.put(location, newModel);
                }
            }
        }
    }
}
