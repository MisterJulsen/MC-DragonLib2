package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.mixin.client;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.GeometryLoaderManager;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.IGeometryLoader;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.IUnbakedGeometry;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.extensions.BlockModelExtensions;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.util.GsonHelper;

@Mixin(BlockModel.Deserializer.class)
public abstract class BlockModelDeserializerMixin {

	@Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;", at = @At("RETURN"), cancellable = true)
	public void modelLoading(JsonElement element, Type type, JsonDeserializationContext deserializationContext, CallbackInfoReturnable<BlockModel> cir) {
		BlockModel model = cir.getReturnValue();
		JsonObject jsonobject = element.getAsJsonObject();
		IUnbakedGeometry<?> geometry = deserializeGeometry(deserializationContext, jsonobject);

		List<BlockElement> elements = model.getElements();
		if (geometry != null) {
			elements.clear();
			((BlockModelExtensions)model).dragonlib$setCustomGeometry(geometry);
		}

		if (jsonobject.has("transform")) {
			JsonObject transform = GsonHelper.getAsJsonObject(jsonobject, "transform");
			((BlockModelExtensions)model).dragonlib$setRootTransform(deserializationContext.deserialize(transform, Transformation.class));
		}

		if (jsonobject.has("visibility")) {
			JsonObject visibility = GsonHelper.getAsJsonObject(jsonobject, "visibility");
			for (Map.Entry<String, JsonElement> part : visibility.entrySet()) {
				((BlockModelExtensions)model).dragonlib$getVisibilityData().setVisibilityState(part.getKey(), part.getValue().getAsBoolean());
			}
		}

	}

	@Unique
	@Nullable
	private static IUnbakedGeometry<?> deserializeGeometry(JsonDeserializationContext deserializationContext, JsonObject object) throws JsonParseException {
		String loaderId = GeometryLoaderManager.getModelLoader(object);
		if (loaderId == null)
			return null;

		ResourceLocation name = new ResourceLocation(loaderId);
		IGeometryLoader<?> loader = GeometryLoaderManager.get(name);
		if (loader == null) {
			if (!GeometryLoaderManager.KNOWN_MISSING_LOADERS.contains(name)) {
				GeometryLoaderManager.KNOWN_MISSING_LOADERS.add(name);
				DragonLib.LOGGER.warn(String.format(Locale.ENGLISH, "Model loader '%s' not found. Registered loaders: %s", name, GeometryLoaderManager.getLoaderList()));
				DragonLib.LOGGER.warn("Falling back to vanilla logic.");
			}
			return null;
		}

		return loader.read(object, deserializationContext);
	}
}
