package de.mrjulsen.mcdragonlib.neoforge.client.model.loaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DLModelExtensionLoader implements IGeometryLoader<DLUnbakedModelExtension> {

    public static final ResourceLocation ID = DLUtils.resourceLocation(DragonLib.MODID, "advanced_json");
    public static final DLModelExtensionLoader INSTANCE = new DLModelExtensionLoader();

    private record DirectionKey(Direction dir) {}

    @Override
    public DLUnbakedModelExtension read(JsonObject json, JsonDeserializationContext ctx) {
        BlockModel vanilla = new BlockModel.Deserializer().deserialize(json, BlockModel.class, ctx);
        Map<DLFaceKey, DLFaceData> faceData = new HashMap<>();

        Map<DirectionKey, MutableInt> faceIndex = new HashMap<>();
        JsonArray elements = json.getAsJsonArray("elements");
        for (int e = 0; e < elements.size(); e++) {
            JsonObject element = elements.get(e).getAsJsonObject();
            JsonObject faces = element.getAsJsonObject("faces");

            for (Map.Entry<String, JsonElement> entry : faces.entrySet()) {
                JsonObject face = entry.getValue().getAsJsonObject();
                Direction cullFace = this.getCullFacing(face);
                MutableInt idx = faceIndex.computeIfAbsent(new DirectionKey(cullFace), c -> new MutableInt(0));
                DLFaceData data = DLFaceData.read(face.getAsJsonObject("dragonlib_data"), DLFaceData.DEFAULT);

                if (data != null) {
                    faceData.put(new DLFaceKey(idx.getAndIncrement(), cullFace), data);
                }
            }
        }

        return new DLUnbakedModelExtension(vanilla, faceData);
    }

    @Nullable
    private Direction getCullFacing(JsonObject json) {
        String s = GsonHelper.getAsString(json, "cullface", "");
        return Direction.byName(s);
    }
}
