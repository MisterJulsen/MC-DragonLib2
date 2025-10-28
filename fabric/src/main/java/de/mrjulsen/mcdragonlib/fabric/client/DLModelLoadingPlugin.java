package de.mrjulsen.mcdragonlib.fabric.client;

import com.google.common.collect.ImmutableMap;

import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry.ModelRegistryData;
import de.mrjulsen.mcdragonlib.fabric.client.model.DynamicBakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DLModelLoadingPlugin implements ModelLoadingPlugin {

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        
        ImmutableMap<ResourceLocation, ModelRegistryData> factories = DLBlockModelRegistry.getCustomRegisteredModelsMapped();

        pluginContext.modifyModelAfterBake().register((original, context) -> {
            if (factories.containsKey(context.id())) {
                ModelRegistryData data = factories.get(context.id());
                DLBlockModelRegistry.setOriginalModel(data.state(), original);
                return new DynamicBakedModel(original, data.state(), data.factory().getModelFactory().get());
            }

            return original;
        });
    }
}