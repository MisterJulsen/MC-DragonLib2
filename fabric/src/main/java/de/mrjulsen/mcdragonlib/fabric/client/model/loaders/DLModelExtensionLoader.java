package de.mrjulsen.mcdragonlib.fabric.client.model.loaders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.IGeometryLoader;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DLModelExtensionLoader implements IGeometryLoader<DLUnbakedModelExtension> {
    public static final ResourceLocation ID = new ResourceLocation("dragonlib", "advanced_json");
    public static final DLModelExtensionLoader INSTANCE = new DLModelExtensionLoader();

    private record DirectionKey(Direction dir) {}

    @Override
    public DLUnbakedModelExtension read(JsonObject json, JsonDeserializationContext ctx) {
        BlockModel vanilla = new Deserializer().deserialize(json, BlockModel.class, ctx);
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

    public static class Deserializer implements JsonDeserializer<BlockModel> {
        public BlockModel deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            List<BlockElement> list = this.getElements(context, jsonObject);
            String string = this.getParentName(jsonObject);
            Map<String, Either<Material, String>> map = this.getTextureMap(jsonObject);
            Boolean boolean_ = this.getAmbientOcclusion(jsonObject);
            ItemTransforms itemTransforms = ItemTransforms.NO_TRANSFORMS;
            if (jsonObject.has("display")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "display");
                itemTransforms = (ItemTransforms)context.deserialize(jsonObject2, ItemTransforms.class);
            }

            List<ItemOverride> list2 = this.getOverrides(context, jsonObject);
            BlockModel.GuiLight guiLight = null;
            if (jsonObject.has("gui_light")) {
                guiLight = BlockModel.GuiLight.getByName(GsonHelper.getAsString(jsonObject, "gui_light"));
            }

            ResourceLocation resourceLocation = string.isEmpty() ? null : new ResourceLocation(string);
            return new BlockModel(resourceLocation, list, map, boolean_, guiLight, itemTransforms, list2);
        }

        protected List<ItemOverride> getOverrides(JsonDeserializationContext context, JsonObject json) {
            List<ItemOverride> list = Lists.newArrayList();
            if (json.has("overrides")) {
                for(JsonElement jsonElement : GsonHelper.getAsJsonArray(json, "overrides")) {
                    list.add((ItemOverride)context.deserialize(jsonElement, ItemOverride.class));
                }
            }

            return list;
        }

        private Map<String, Either<Material, String>> getTextureMap(JsonObject json) {
            ResourceLocation resourceLocation = TextureAtlas.LOCATION_BLOCKS;
            Map<String, Either<Material, String>> map = Maps.newHashMap();
            if (json.has("textures")) {
                JsonObject jsonObject = GsonHelper.getAsJsonObject(json, "textures");

                for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    map.put((String)entry.getKey(), parseTextureLocationOrReference(resourceLocation, ((JsonElement)entry.getValue()).getAsString()));
                }
            }

            return map;
        }

        static boolean isTextureReference(String str) {
            return str.charAt(0) == '#';
        }

        private static Either<Material, String> parseTextureLocationOrReference(ResourceLocation location, String name) {
            if (isTextureReference(name)) {
                return Either.right(name.substring(1));
            } else {
                ResourceLocation resourceLocation = ResourceLocation.tryParse(name);
                if (resourceLocation == null) {
                    throw new JsonParseException(name + " is not valid resource location");
                } else {
                    return Either.left(new Material(location, resourceLocation));
                }
            }
        }

        private String getParentName(JsonObject json) {
            return GsonHelper.getAsString(json, "parent", "");
        }

        @Nullable
        protected Boolean getAmbientOcclusion(JsonObject json) {
            return json.has("ambientocclusion") ? GsonHelper.getAsBoolean(json, "ambientocclusion") : null;
        }

        protected List<BlockElement> getElements(JsonDeserializationContext context, JsonObject json) {
            List<BlockElement> list = Lists.newArrayList();
            if (json.has("elements")) {
                for(JsonElement jsonElement : GsonHelper.getAsJsonArray(json, "elements")) {
                    list.add((BlockElement)context.deserialize(jsonElement, BlockElement.class));
                }
            }

            return list;
        }
    }
}
