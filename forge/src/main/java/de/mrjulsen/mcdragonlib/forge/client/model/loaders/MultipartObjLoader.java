package de.mrjulsen.mcdragonlib.forge.client.model.loaders;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.forge.client.model.loaders.MultipartObjModel.ModelGroup;
import de.mrjulsen.mcdragonlib.forge.client.model.loaders.MultipartObjModel.ModelObject;
import de.mrjulsen.mcdragonlib.forge.client.model.loaders.MultipartObjModel.ModelSettings;
import de.mrjulsen.mcdragonlib.forge.client.model.loaders.MultipartObjModel.SubModelSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.obj.ObjMaterialLibrary;
import net.minecraftforge.client.model.obj.ObjTokenizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A loader for {@link MultipartObjModel OBJ models}.
 * <p>
 * Allows the user to enable automatic face culling, toggle quad shading, flip
 * UVs, render emissively and specify a
 * {@link ObjMaterialLibrary material library} override.
 */
public class MultipartObjLoader implements IGeometryLoader<MultipartObjModel>, ResourceManagerReloadListener {
    public static MultipartObjLoader INSTANCE = new MultipartObjLoader();
    public static final ResourceLocation ID = new ResourceLocation(DragonLib.MODID, "obj");

    private final Map<MultipartObjModel.ModelSettings, MultipartObjModel> modelCache = Maps.newConcurrentMap();
    private final Map<ResourceLocation, ObjMaterialLibrary> materialCache = Maps.newConcurrentMap();

    private ResourceManager manager = Minecraft.getInstance().getResourceManager();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        modelCache.clear();
        materialCache.clear();
        manager = resourceManager;
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
            new ResourceLocation(modelLocation),
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
                readSubModel(new ResourceLocation(subModelSettings.model())) :
                loadModel(new ModelSettings(new ResourceLocation(subModelSettings.model()), automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride, List.of()))
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

    private MultipartObjModel readSubModel(ResourceLocation location) {        
        try {
            Resource resource = manager.getResource(location).orElseThrow();
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
            Resource resource = manager.getResource(settings.modelLocation()).orElseThrow();
            try (ObjTokenizer tokenizer = new ObjTokenizer(resource.open())) {
                return MultipartObjModel.parse(tokenizer, settings);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Could not find OBJ model", e);
            } catch (Exception e) {
                throw new RuntimeException("Could not read OBJ model", e);
            }
        });
    }

    public ObjMaterialLibrary loadMaterialLibrary(ResourceLocation materialLocation) {
        return materialCache.computeIfAbsent(materialLocation, (location) -> {
            Resource resource = manager.getResource(location).orElseThrow();
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
