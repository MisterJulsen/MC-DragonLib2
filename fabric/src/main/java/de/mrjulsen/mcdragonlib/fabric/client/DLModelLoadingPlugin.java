package de.mrjulsen.mcdragonlib.fabric.client;

import com.google.common.collect.ImmutableMap;

import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry.ModelRegistryData;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents.ModifyModels;
import de.mrjulsen.mcdragonlib.events.client.ModelReplacements;
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
        ModelEvents.ADDITIONAL_MODELS.invoker().registerAdditionalModels(e -> pluginContext.addModels(e));

        ModelReplacements replacements = ModelReplacements.collect();
        if (!replacements.isEmpty()) {
            pluginContext.modifyModelAfterBake().register((original, ctx) -> {
                ResourceLocation id = ctx.id();
                if (id == null) {
                    return original;
                }

                BakedModel model = replacements.apply(id, loc -> original, new ModifyModels.Context() {
                    @Override
                    public ModelBakery getModelBakery() {
                        return ctx.loader();
                    }

                    @Override
                    public ResourceLocation getModelLocation() {
                        return id;
                    }

                    @Override
                    public UnbakedModel getUnbakedModel() {
                        return ctx.sourceModel();
                    }
                });
                return model == null ? original : model;
            });
        }

        ImmutableMap<ResourceLocation, ModelRegistryData> factories = DLBlockModelRegistry.getCustomRegisteredModelsMapped();
        if (!factories.isEmpty()) {
            pluginContext.modifyModelAfterBake().register((original, ctx) -> {
                ResourceLocation id = ctx.id();
                if (id == null) {
                    return original;
                }

                ModelRegistryData data = factories.get(id);
                if (data == null) {
                    return original;
                }

                DLBlockModelRegistry.setOriginalModel(data.state(), original);
                return new DynamicBakedModel(original, data.state(), data.factory().getModelFactory().get());
            });
        }
    }
}
