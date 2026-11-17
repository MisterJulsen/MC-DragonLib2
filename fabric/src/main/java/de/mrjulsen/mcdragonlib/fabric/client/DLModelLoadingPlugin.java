/*
 * DragonLib 3 - 1.20.1
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

                DLBlockModelRegistry.setOriginalModel(data.factory().getType(), data.state(), original);
                return new DynamicBakedModel(original, data.state(), data.factory().getModelFactory().get());
            });
        }
    }
}
