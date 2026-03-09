package de.mrjulsen.mcdragonlib.neoforge.client.model.loaders;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mrjulsen.mcdragonlib.DragonLib;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class ObjLoader implements IGeometryLoader<ObjModel>, ResourceManagerReloadListener {
   public static ObjLoader INSTANCE = new ObjLoader();

   private final Map<ObjModel.ModelSettings, ObjModel> modelCache = Maps.newConcurrentMap();
   private final Map<ResourceLocation, ObjMaterialLibrary> materialCache = Maps.newConcurrentMap();

   @Override
   public void onResourceManagerReload(ResourceManager resourceManager) {
      this.modelCache.clear();
      this.materialCache.clear();
   }

   // -------------------------------------------------------------------------
   // IGeometryLoader entry point
   // -------------------------------------------------------------------------

   @Override
   public ObjModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
      return readInternal(deserializationContext, jsonObject);
   }

   /**
    * Reads the main model JSON and merges any sub-models declared in the "add" array.
    * This is the single entry point for both the top-level model and recursively
    * referenced JSON sub-models.
    */
   public ObjModel readInternal(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      if (!modelContents.has("model")) {
         throw new JsonParseException("OBJ Loader requires a 'model' key that points to a valid .OBJ model.");
      }

      String modelLocation = modelContents.get("model").getAsString();
      boolean automaticCulling = GsonHelper.getAsBoolean(modelContents, "automatic_culling", true);
      boolean shadeQuads       = GsonHelper.getAsBoolean(modelContents, "shade_quads", true);
      boolean flipV             = GsonHelper.getAsBoolean(modelContents, "flip_v", false);
      boolean emissiveAmbient  = GsonHelper.getAsBoolean(modelContents, "emissive_ambient", true);
      String  mtlOverride       = GsonHelper.getAsString(modelContents, "mtl_override", null);

      // Collect sub-model settings from the optional "add" array
      List<ObjModel.SubModelSettings> subSettingsList = new ArrayList<>();
      if (modelContents.has("add")) {
         for (JsonElement element : GsonHelper.getAsJsonArray(modelContents, "add")) {
            subSettingsList.add(DragonLib.GSON.fromJson(element, ObjModel.SubModelSettings.class));
         }
      }

      ObjModel.ModelSettings settings = new ObjModel.ModelSettings(
         ResourceLocation.parse(modelLocation),
         automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride
      );

      // Always get the clean cached base model, then wrap it so we never mutate the cache.
      ObjModel baseModel = loadModel(settings);

      if (subSettingsList.isEmpty()) {
         return baseModel;
      }

      // Create a fresh composite model that starts with all parts of the base model,
      // but is NOT stored in the cache — so each JSON variant gets its own instance.
      ObjModel model = baseModel.copyForComposition();

      // Merge sub-models
      int subIndex = 0;
      for (ObjModel.SubModelSettings subModelSettings : subSettingsList) {
         subIndex++;
         ObjModel subModel = subModelSettings.isJson()
            ? readSubModel(ResourceLocation.parse(subModelSettings.model()))
            : loadModel(new ObjModel.ModelSettings(
                  ResourceLocation.parse(subModelSettings.model()),
                  automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride
               ));

         if (subModel == null) continue;

         for (Object part : subModel.getParts()) {
            if (!(part instanceof ObjModel.ModelGroup group)) continue;
            if (!group.isInheritable()) continue;
            String newName = "m" + subIndex + "_" + group.name();
            model.addPart(newName, group.copy(subModelSettings, newName));
         }
      }

      return model;
   }

   // -------------------------------------------------------------------------
   // Helpers
   // -------------------------------------------------------------------------

   private static ResourceManager getResourceManager() {
      return Minecraft.getInstance().getResourceManager();
   }

   /**
    * Loads and parses a JSON-based sub-model (i.e. another model JSON that itself
    * uses this loader). Used for nested ".json" entries in the "add" array.
    */
   private ObjModel readSubModel(ResourceLocation location) {
      try {
         Resource resource = getResourceManager().getResource(location).orElseThrow();
         InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8);
         JsonObject obj = GsonHelper.parse(reader);
         return readInternal(null, obj);
      } catch (IOException e) {
         DragonLib.LOGGER.error("Could not read sub-model JSON at {}: {}", location, e.getMessage());
      }
      return null;
   }

   /**
    * Loads (or returns from cache) a plain OBJ model described by {@code settings}.
    */
   public ObjModel loadModel(ObjModel.ModelSettings settings) {
      return this.modelCache.computeIfAbsent(settings, data -> {
         Resource resource = getResourceManager().getResource(settings.modelLocation()).orElseThrow();
         try (ObjTokenizer tokenizer = new ObjTokenizer(resource.open())) {
            return ObjModel.parse(tokenizer, settings);
         } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find OBJ model", e);
         } catch (Exception e) {
            throw new RuntimeException("Could not read OBJ model", e);
         }
      });
   }

   /**
    * Loads (or returns from cache) a material library at {@code materialLocation}.
    */
   public ObjMaterialLibrary loadMaterialLibrary(ResourceLocation materialLocation) {
      return this.materialCache.computeIfAbsent(materialLocation, location -> {
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
