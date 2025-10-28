package de.mrjulsen.mcdragonlib.fabric.client;

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.RegisterGeometryLoadersCallback;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.MultipartObjLoader;
import de.mrjulsen.mcdragonlib.fabric.client.model.obj.ObjLoader;
import net.fabricmc.api.ClientModInitializer;
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
    }
}
