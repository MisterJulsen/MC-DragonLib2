/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
import de.mrjulsen.mcdragonlib.client.util.SmoothTextRenderTypes;
import de.mrjulsen.mcdragonlib.mixin.client.ModelBakeryAccessor;
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
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

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
        // NeoForge only accepts additional models with the standalone variant
        ModelEvents.ADDITIONAL_MODELS.invoker().registerAdditionalModels(model -> event.register(ModelResourceLocation.standalone(model)));
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
                    DLBlockModelRegistry.setOriginalModel(r.getType(), state, originalModel);
                    DynamicBakedModel newModel = new DynamicBakedModel(originalModel, state, r.getModelFactory().get());
                    registry.put(location, newModel);
                }
            }
        }
    }


    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), SmoothTextRenderTypes.SHADER_ID, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), SmoothTextRenderTypes::setShader);
        } catch (Exception e) {
            SmoothTextRenderTypes.onShaderLoadFailed(SmoothTextRenderTypes.SHADER_ID, e);
        }
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), SmoothTextRenderTypes.INTENSITY_SHADER_ID, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), SmoothTextRenderTypes::setIntensityShader);
        } catch (Exception e) {
            SmoothTextRenderTypes.onShaderLoadFailed(SmoothTextRenderTypes.INTENSITY_SHADER_ID, e);
        }
    }
}
