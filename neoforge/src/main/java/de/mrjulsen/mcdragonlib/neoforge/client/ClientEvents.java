package de.mrjulsen.mcdragonlib.neoforge.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry.ICustomModelFactory;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel.ModelType;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents.ModifyModels;
import de.mrjulsen.mcdragonlib.events.client.ModelReplacements;
import de.mrjulsen.mcdragonlib.mixin.ModelBakeryAccessor;
import de.mrjulsen.mcdragonlib.neoforge.client.model.DynamicBakedModel;
import de.mrjulsen.mcdragonlib.neoforge.client.model.loaders.DLModelExtensionLoader;
import de.mrjulsen.mcdragonlib.neoforge.client.model.loaders.ObjLoader;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = DragonLib.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    private static final class MutableContext implements ModifyModels.Context {

        private final ModelEvent.ModifyBakingResult event;
        private ModelResourceLocation location;

        private MutableContext(ModelEvent.ModifyBakingResult event) {
            this.event = event;
        }

        @Override
        public ModelBakery getModelBakery() {
            return event.getModelBakery();
        }

        @Override
        public ModelResourceLocation getModelLocation() {
            return location;
        }

        @Override
        public UnbakedModel getUnbakedModel() {
            return ((ModelBakeryAccessor)event.getModelBakery()).dragonlib$getModel(location.id());
        }
    }


    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(DLUtils.resourceLocation(DragonLib.MODID, "multipart_obj"), ObjLoader.INSTANCE);
        event.register(DLUtils.resourceLocation(DragonLib.MODID, "advanced_json"), DLModelExtensionLoader.INSTANCE);
    }


    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        ModelEvents.ADDITIONAL_MODELS.invoker().registerAdditionalModels(event::register);
    }


    @SubscribeEvent
    public static void onModifyBakingResult(final ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> registry = event.getModels();
        applyModelReplacements(event, registry);
        replaceRegisteredModels(registry);
    }

    private static void applyModelReplacements(final ModelEvent.ModifyBakingResult event, Map<ModelResourceLocation, BakedModel> registry) {
        ModelReplacements replacements = ModelReplacements.collect();
        if (replacements.isEmpty()) {
            return;
        }

        MutableContext context = new MutableContext(event);
        Function<ModelResourceLocation, BakedModel> lookup = registry::get;
        Map<ModelResourceLocation, BakedModel> modified = new HashMap<>();

        if (replacements.hasFilters()) {
            for (ModelResourceLocation location : registry.keySet()) {
                context.location = location;
                BakedModel model = replacements.apply(location, lookup, context);
                if (model != null) {
                    modified.put(location, model);
                }
            }
        } else {
            for (ModelResourceLocation location : replacements.namedLocations()) {
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


    private static void replaceRegisteredModels(Map<ModelResourceLocation, BakedModel> registry) {
        Queue<ICustomModelFactory> replacements = DLBlockModelRegistry.getCustomRegisteredModels(registry);
        while (!replacements.isEmpty()) {
            ICustomModelFactory r = replacements.poll();

            if (r.getModelFactory() != null && !r.getStates().isEmpty()) {
                for (BlockState state : r.getStates()) {
                    ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
                    if (r.getType() == ModelType.ITEM) {
                        location = new ModelResourceLocation(location.id(), "inventory");
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
