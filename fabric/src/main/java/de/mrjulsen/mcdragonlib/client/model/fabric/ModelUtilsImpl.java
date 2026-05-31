package de.mrjulsen.mcdragonlib.client.model.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class ModelUtilsImpl {

    public static BakedModel getModel(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(location);
    }

    public static BakedModel getModel(ModelResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(location);
    }
}
