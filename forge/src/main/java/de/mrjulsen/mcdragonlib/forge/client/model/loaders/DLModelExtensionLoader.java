/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.forge.client.model.loaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.ForgeFaceData;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DLModelExtensionLoader implements IGeometryLoader<DLUnbakedModelExtension> {

    public static final ResourceLocation ID = new ResourceLocation(DragonLib.MODID, "advanced_json");
    public static final DLModelExtensionLoader INSTANCE = new DLModelExtensionLoader();

    private record DirectionKey(Direction dir) {}

    @Override
    public DLUnbakedModelExtension read(JsonObject json, JsonDeserializationContext ctx) {
        BlockModel vanilla = new BlockModel.Deserializer().deserialize(json, BlockModel.class, ctx);
        Map<DLFaceKey, DLFaceData> faceData = new HashMap<>();

        Map<DirectionKey, MutableInt> faceIndex = new HashMap<>();
        JsonArray elements = json.has("elements") ? json.getAsJsonArray("elements") : new JsonArray();
        for (int e = 0; e < elements.size(); e++) {
            JsonObject element = elements.get(e).getAsJsonObject();
            JsonObject faces = element.getAsJsonObject("faces");
            if (faces == null) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                JsonElement faceJson = faces.get(direction.getName());
                if (faceJson == null) {
                    continue;
                }
                JsonObject face = faceJson.getAsJsonObject();
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
