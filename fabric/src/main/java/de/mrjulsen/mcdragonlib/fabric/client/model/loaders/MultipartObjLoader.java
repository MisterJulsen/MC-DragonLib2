package de.mrjulsen.mcdragonlib.fabric.client.model.loaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.IGeometryLoader;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.MultipartObjModel.ModelGroup;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.MultipartObjModel.ModelObject;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.MultipartObjModel.ModelSettings;
import de.mrjulsen.mcdragonlib.fabric.client.model.loaders.MultipartObjModel.SubModelSettings;
import de.mrjulsen.mcdragonlib.fabric.client.model.obj.ObjMaterialLibrary;
import de.mrjulsen.mcdragonlib.fabric.client.model.obj.ObjTokenizer;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

public class MultipartObjLoader implements ModelLoadingPlugin, IGeometryLoader<MultipartObjModel> {
    public static final ResourceLocation ID = DLUtils.resourceLocation(DragonLib.MODID, "multipart_obj");
	public static final MultipartObjLoader INSTANCE = new MultipartObjLoader();

	private final Map<MultipartObjModel.ModelSettings, MultipartObjModel> modelCache = Maps.newConcurrentMap();
	private final Map<ResourceLocation, ObjMaterialLibrary> materialCache = Maps.newConcurrentMap();
    
    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        modelCache.clear();
        materialCache.clear();
    }

    @Override
    public MultipartObjModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
        return readInternal(deserializationContext, jsonObject);
    }

    public MultipartObjModel readInternal(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        if (!modelContents.has("model"))
            throw new JsonParseException("OBJ Loader requires a 'model' key that points to a valid .OBJ model.");
        if (!modelContents.has("loader") || !modelContents.get("loader").getAsString().equals(ID.toString()))
            throw new RuntimeException("Importing invalid model loader.");

        String modelLocation = modelContents.get("model").getAsString();

        boolean automaticCulling = GsonHelper.getAsBoolean(modelContents, "automatic_culling", true);
        boolean shadeQuads = GsonHelper.getAsBoolean(modelContents, "shade_quads", true);
        boolean flipV = GsonHelper.getAsBoolean(modelContents, "flip_v", false);
        boolean emissiveAmbient = GsonHelper.getAsBoolean(modelContents, "emissive_ambient", true);
        String mtlOverride = GsonHelper.getAsString(modelContents, "mtl_override", null);

        List<SubModelSettings> subSettings = new ArrayList<>();
        if (modelContents.has("add")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(modelContents, "add")) {
                subSettings.add(DragonLib.GSON.fromJson(element, SubModelSettings.class));
            }
        }

        MultipartObjModel.ModelSettings settings = new MultipartObjModel.ModelSettings(
                DLUtils.resourceLocation(modelLocation),
            automaticCulling,
            shadeQuads,
            flipV,
            emissiveAmbient,
            mtlOverride,
            subSettings
        );
        MultipartObjModel model = loadModel(settings);

        int i = 0;
        for (SubModelSettings subModelSettings : settings.subSettings()) {
            i++;
            MultipartObjModel subModel = subModelSettings.isJson() ? 
                readSubModel(DLUtils.resourceLocation(subModelSettings.model())) :
                loadModel(new ModelSettings(DLUtils.resourceLocation(subModelSettings.model()), automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride, List.of()))
            ;
            for (ModelObject part : subModel.getParts()) {
                ModelGroup group = ((ModelGroup)part);
                if (!group.settings.inheritable()) continue;
                String newName = "m" + i + "_" + part.name();
                group = group.copy(subModelSettings, newName);
                model.addPart(group.name(), group);
            }
        }

        return model;
    }

	private static ResourceManager getResourceManager() {
		return Minecraft.getInstance().getResourceManager();
	}

    private MultipartObjModel readSubModel(ResourceLocation location) {        
        try {
            Resource resource = getResourceManager().getResource(location).orElseThrow();
            InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8);
            JsonObject obj = GsonHelper.parse(reader);
            return readInternal(null, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public MultipartObjModel loadModel(MultipartObjModel.ModelSettings settings) {
        return modelCache.computeIfAbsent(settings, (data) -> {
            Resource resource = getResourceManager().getResource(settings.modelLocation()).orElseThrow();
            try (ObjTokenizer tokenizer = new ObjTokenizer(resource.open())) {
                return MultipartObjParser.parse(tokenizer, settings);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Could not find OBJ model", e);
            } catch (Exception e) {
                throw new RuntimeException("Could not read OBJ model", e);
            }
        });
    }

    public ObjMaterialLibrary loadMaterialLibrary(ResourceLocation materialLocation) {
        return materialCache.computeIfAbsent(materialLocation, (location) -> {
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
}