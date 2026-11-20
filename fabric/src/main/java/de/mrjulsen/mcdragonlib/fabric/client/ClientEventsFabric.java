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

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.RegisterGeometryLoadersCallback;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.DLModelExtensionLoader;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.MultipartObjLoader;
import de.mrjulsen.mcdragonlib.fabric.client.model.obj.ObjLoader;
import de.mrjulsen.mcdragonlib.client.util.SmoothTextDeferred;
import de.mrjulsen.mcdragonlib.client.util.SmoothTextRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

@Environment(EnvType.CLIENT)
public class ClientEventsFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new DLModelLoadingPlugin());
		RegisterGeometryLoadersCallback.EVENT.register(loaders -> loaders.put(ObjLoader.ID, ObjLoader.INSTANCE));
		RegisterGeometryLoadersCallback.EVENT.register(loaders -> loaders.put(MultipartObjLoader.ID, MultipartObjLoader.INSTANCE));
		RegisterGeometryLoadersCallback.EVENT.register(loaders -> loaders.put(DLModelExtensionLoader.ID, DLModelExtensionLoader.INSTANCE));

		CoreShaderRegistrationCallback.EVENT.register(context -> {
			try {
				context.register(SmoothTextRenderTypes.SHADER_ID, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, SmoothTextRenderTypes::setShader);
			} catch (Exception e) {
				SmoothTextRenderTypes.onShaderLoadFailed(SmoothTextRenderTypes.SHADER_ID, e);
			}
			try {
				context.register(SmoothTextRenderTypes.INTENSITY_SHADER_ID, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, SmoothTextRenderTypes::setIntensityShader);
			} catch (Exception e) {
				SmoothTextRenderTypes.onShaderLoadFailed(SmoothTextRenderTypes.INTENSITY_SHADER_ID, e);
			}
		});

		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> SmoothTextDeferred.draw());
    }
}
