package de.mrjulsen.mcdragonlib.fabric.client;

import com.google.common.collect.ImmutableMap;

import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry.ModelRegistryData;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents;
import de.mrjulsen.mcdragonlib.fabric.client.model.DynamicBakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DLModelLoadingPlugin implements ModelLoadingPlugin {

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        ModelEvents.ADDITIONAL_MODELS.invoker().registerAdditionalModels(pluginContext::addModels);
        pluginContext.modifyModelAfterBake().register((original, ctx) -> {
            BakedModel model = ModelEvents.MODIFY_MODELS.invoker().modifyModels(original, new ModelEvents.ModifyModels.Context() {
                @Override
                public ModelBakery getModelBakery() {
                    return ctx.loader();
                }

                @Override
                public ResourceLocation getModelLocation() {
                    return ctx.id();
                }

                @Override
                public UnbakedModel getUnbakedModel() {
                    return ctx.sourceModel();
                }
            });
            return model == null ? original : model;
        });

        ImmutableMap<ResourceLocation, ModelRegistryData> factories = DLBlockModelRegistry.getCustomRegisteredModelsMapped();

        pluginContext.modifyModelAfterBake().register((original, context) -> {
            BakedModel model = original;
            if (factories.containsKey(context.id())) {
                ModelRegistryData data = factories.get(context.id());
                DLBlockModelRegistry.setOriginalModel(data.state(), model);
                model = new DynamicBakedModel(model, data.state(), data.factory().getModelFactory().get());
            }
            return model;
        });
    }
}