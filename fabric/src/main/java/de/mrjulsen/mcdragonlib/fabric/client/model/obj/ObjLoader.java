package de.mrjulsen.mcdragonlib.fabric.client.model.obj;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.IGeometryLoader;
import de.mrjulsen.mcdragonlib.fabric.client.model.obj.ObjModel.ModelSettings;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

public class ObjLoader implements ModelLoadingPlugin, IGeometryLoader<ObjModel> {
	public static final ResourceLocation ID = DLUtils.resourceLocation("forge:obj");
	public static final ObjLoader INSTANCE = new ObjLoader();
	public static final String OBJ_MARKER = DLUtils.resourceLocation(DragonLib.MODID, "obj_marker").toString();

	private final Map<ModelSettings, ObjModel> modelCache = Maps.newConcurrentMap();
	private final Map<ResourceLocation, ObjMaterialLibrary> materialCache = Maps.newConcurrentMap();

	@Override
	public void onInitializeModelLoader(Context ctx) {
		
		modelCache.clear();
		materialCache.clear();

		findModels(ctx::addModels);
		ctx.resolveModel().register(new Resolver());
	}

		private void findModels(Consumer<ResourceLocation> out) {
		ResourceManager manager = getResourceManager();
		manager.listResources("models/misc", id -> {
			if (id.getPath().endsWith(".json")) {
				manager.getResource(id).ifPresent(resource -> {
					if (tryLoadModelJson(id, resource) != null) {
						out.accept(id);
					}
				});
			}
			return true;
		});
	}

	private JsonObject tryLoadModelJson(ResourceLocation id, Resource resource) {
		try {
			JsonObject json = JsonParser.parseReader(resource.openAsReader()).getAsJsonObject();
			if (json.has(OBJ_MARKER)) {
				return json;
			}
		} catch (IOException | IllegalStateException e) {
			DragonLib.LOGGER.error("Error loading obj model from models/misc: " + id, e);
		}
		return null;
	}

	private Either<ModelSettings, RuntimeException> tryReadSettings(JsonObject json) {
		try {
			ResourceLocation objLocation = DLUtils.resourceLocation(GsonHelper.getAsString(json, "model"));
			return Either.left(new ModelSettings(objLocation,
					GsonHelper.getAsBoolean(json, "automaticCulling", true),
					GsonHelper.getAsBoolean(json, "shadeQuads", true),
					GsonHelper.getAsBoolean(json, "flipV", true),
					GsonHelper.getAsBoolean(json, "emissiveAmbient", true),
					GsonHelper.getAsString(json, "mtlOverride", null)
			));
		} catch (RuntimeException e) { 
			return Either.right(e);
		}
	}

		@Override
	public ObjModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
		return tryReadSettings(jsonObject).map(this::loadModel, exception -> {
			throw new JsonParseException("Error loading OBJ model settings", exception);
		});
	}

	private ObjModel loadModel(ObjModel.ModelSettings settings) {
		ResourceLocation id = settings.modelLocation();
		Resource resource = getResourceManager().getResource(id).orElseThrow(() -> new NoSuchElementException(id.toString()));
		return loadModel(resource, settings);
	}

	private ObjModel loadModel(Resource resource, ObjModel.ModelSettings settings) {
		return modelCache.computeIfAbsent(settings, data -> {
			try (ObjTokenizer tokenizer = new ObjTokenizer(resource.open())) {
				return ObjParser.parse(tokenizer, settings);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Could not find OBJ model", e);
			} catch (Exception e) {
				throw new RuntimeException("Could not read OBJ model", e);
			}
		});
	}

	public ObjMaterialLibrary loadMaterialLibrary(ResourceLocation materialLocation) {
		return materialCache.computeIfAbsent(materialLocation, location -> {
			Resource resource = getResourceManager().getResource(location).orElseThrow();
			try (ObjTokenizer rdr = new ObjTokenizer(resource.open())) {
				return new ObjMaterialLibrary(rdr);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Could not find OBJ material library", e);
			} catch (Exception e) {
				throw new RuntimeException("Could not read OBJ material library", e);
			}
		});
	}

	private static ResourceManager getResourceManager() {
		return Minecraft.getInstance().getResourceManager();
	}

	private class Resolver implements ModelResolver {
		@Override
		@Nullable
		public UnbakedModel resolveModel(Context context) {
			ResourceLocation id = context.id();
			var resourceManager = getResourceManager();
			return resourceManager.getResource(id).map(resource -> {
				JsonObject json = tryLoadModelJson(id, resource);
				if (json != null) {
					return tryReadSettings(json).map(settings -> {
						try {
							return loadModel(resourceManager.getResourceOrThrow(DLUtils.resourceLocation(GsonHelper.getAsString(json, "model"))), settings);
						} catch (FileNotFoundException e) {
							throw new RuntimeException(e);
						}
					}, exception -> {
						DragonLib.LOGGER.error("Error loading obj model: " + id, exception);
						return null;
					});

				}
				return null;
			}).orElse(null);
		}
	}
}
