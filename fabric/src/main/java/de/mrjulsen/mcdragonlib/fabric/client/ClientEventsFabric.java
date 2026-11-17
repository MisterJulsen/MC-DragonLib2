package de.mrjulsen.mcdragonlib.fabric.client;

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.RegisterGeometryLoadersCallback;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.DLModelExtensionLoader;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.MultipartObjLoader;
import de.mrjulsen.mcdragonlib.fabric.client.model.obj.ObjLoader;
import de.mrjulsen.mcdragonlib.client.util.SmoothTextRenderTypes;
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
    }
}
