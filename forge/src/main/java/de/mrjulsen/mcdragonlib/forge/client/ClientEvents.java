package de.mrjulsen.mcdragonlib.forge.client;

import java.util.Map;
import java.util.Queue;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry.ICustomModelFactory;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel.ModelType;
import de.mrjulsen.mcdragonlib.forge.client.model.DynamicBakedModel;
import de.mrjulsen.mcdragonlib.forge.client.model.loaders.DLModelExtensionLoader;
import de.mrjulsen.mcdragonlib.forge.client.model.loaders.MultipartObjLoader;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}
    

    @SubscribeEvent
    public static void registerGeometryLoaders(RegisterGeometryLoaders event) {
        event.register("multipart_obj", MultipartObjLoader.INSTANCE);
        event.register("advanced_json", DLModelExtensionLoader.INSTANCE);
    }


    @SubscribeEvent
    public static void onModifyBakingResult(final ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> registry = event.getModels();

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
