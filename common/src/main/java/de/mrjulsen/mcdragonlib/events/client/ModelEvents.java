package de.mrjulsen.mcdragonlib.events.client;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public interface ModelEvents {
    Event<AdditionalModels> ADDITIONAL_MODELS = EventFactory.createLoop();
    Event<ModifyModels> MODIFY_MODELS = EventFactory.createLoop();

    interface AdditionalModels {
        void registerAdditionalModels(AdditionalModelsRegistry registry);

        @FunctionalInterface
        interface AdditionalModelsRegistry {
            void register(ModelResourceLocation model);
        }
    }

    @FunctionalInterface
    interface ModifyModels {
        BakedModel modifyModels(BakedModel originalModel, Context context);

        interface Context {
            ModelBakery getModelBakery();
            ModelResourceLocation getModelLocation();
            UnbakedModel getUnbakedModel();
        }
    }
}
