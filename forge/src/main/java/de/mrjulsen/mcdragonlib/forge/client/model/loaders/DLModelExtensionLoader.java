package de.mrjulsen.mcdragonlib.forge.client.model.loaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DLModelExtensionLoader implements IGeometryLoader<DLUnbakedModelExtension> {

    public static final ResourceLocation ID = new ResourceLocation(DragonLib.MODID, "advanced_json");
    public static final DLModelExtensionLoader INSTANCE = new DLModelExtensionLoader();

    @Override
    public DLUnbakedModelExtension read(JsonObject json, JsonDeserializationContext ctx) {
        BlockModel vanilla = new BlockModel.Deserializer().deserialize(json, BlockModel.class, ctx);
        Map<DLFaceKey, DLFaceData> faceData = new HashMap<>();

        JsonArray elements = json.getAsJsonArray("elements");
        for (int e = 0; e < elements.size(); e++) {
            JsonObject element = elements.get(e).getAsJsonObject();
            JsonObject faces = element.getAsJsonObject("faces");

            for (Direction dir : Direction.values()) {
                String name = dir.getName();
                if (!faces.has(name)) continue;

                JsonObject face = faces.getAsJsonObject(name);

                if (face.has("dragonlib_data")) {
                    JsonObject data = face.getAsJsonObject("dragonlib_data");

                    boolean emissive = data.has("emissive") && data.get("emissive").getAsBoolean();
                    boolean ao = !data.has("ambient_occlusion") || data.get("ambient_occlusion").getAsBoolean();

                    List<String> tags = new ArrayList<>();
                    if (data.has("tags")) {
                        for (var el : data.getAsJsonArray("tags")) {
                            tags.add(el.getAsString());
                        }
                    }

                    faceData.put(new DLFaceKey(e, dir), new DLFaceData(ao, emissive, tags));
                }
            }
        }

        return new DLUnbakedModelExtension(vanilla, faceData);
    }
}
