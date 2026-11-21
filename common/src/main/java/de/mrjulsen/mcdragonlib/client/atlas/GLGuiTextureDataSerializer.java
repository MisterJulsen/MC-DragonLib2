/*
 * DragonLib 3 - 1.21.1
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

package de.mrjulsen.mcdragonlib.client.atlas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.ScaleType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Map;

@Deprecated(forRemoval = true)
public class GLGuiTextureDataSerializer implements MetadataSectionSerializer<GLGuiTextureData> {

	@Deprecated(forRemoval = true)
	@Override
	public GLGuiTextureData fromJson(JsonObject json) {
		Map<String, AbstractSprite> result = new HashMap<>();
		
		JsonArray textureSizeJson = GsonHelper.getAsJsonArray(json, "texture_size");
		int[] textureSize = new int[2];
		for (int i = 0; i < textureSize.length; i++) {
			textureSize[i] = GsonHelper.convertToInt(textureSizeJson.get(i), "texture_size[" + i + "]");
		}

		JsonObject sprites = GsonHelper.getAsJsonObject(json, "sprites");
		for (Map.Entry<String, JsonElement> entry : sprites.entrySet()) {
			String key = entry.getKey();
			JsonObject innerObject = GsonHelper.convertToJsonObject(entry.getValue(), key);

			String typeString = GsonHelper.getAsString(innerObject, "type");
			ScaleType type = ScaleType.getByName(typeString);
			
			AbstractSprite sprite = GLGuiTextureData.EMPTY_SPRITE;
			switch (type) {
				case STRETCH -> {
					JsonArray uvArrayJson = GsonHelper.getAsJsonArray(innerObject, "uv", new JsonArray(2));
					int[] uvArray = new int[2];
					for (int i = 0; i < uvArray.length; i++) {
						uvArray[i] = GsonHelper.convertToInt(uvArrayJson.get(i), "uv[" + i + "]");
					}

					JsonArray sizeArrayJson = GsonHelper.getAsJsonArray(innerObject, "size", new JsonArray(2));
					int[] sizeArray = new int[2];
					for (int i = 0; i < sizeArray.length; i++) {
						sizeArray[i] = GsonHelper.convertToInt(sizeArrayJson.get(i), "size[" + i + "]");
					}
					sprite = new GLGuiTextureData.StretchedSprite(uvArray, sizeArray);
				}
				case TILE -> {
					JsonArray uvArrayJson = GsonHelper.getAsJsonArray(innerObject, "uv", new JsonArray(2));
					int[] uvArray = new int[2];
					for (int i = 0; i < uvArray.length; i++) {
						uvArray[i] = GsonHelper.convertToInt(uvArrayJson.get(i), "uv[" + i + "]");
					}

					JsonArray sizeArrayJson = GsonHelper.getAsJsonArray(innerObject, "size", new JsonArray(2));
					int[] sizeArray = new int[2];
					for (int i = 0; i < sizeArray.length; i++) {
						sizeArray[i] = GsonHelper.convertToInt(sizeArrayJson.get(i), "size[" + i + "]");
					}
					sprite = new GLGuiTextureData.TiledSprite(uvArray, sizeArray);
				}
				case NINE_SLICE -> {
					JsonArray uvArrayJson = GsonHelper.getAsJsonArray(innerObject, "uv", new JsonArray(2));
					int[] uvArray = new int[2];
					for (int i = 0; i < uvArray.length; i++) {
						uvArray[i] = GsonHelper.convertToInt(uvArrayJson.get(i), "uv[" + i + "]");
					}

					JsonArray sizeArrayJson = GsonHelper.getAsJsonArray(innerObject, "size", new JsonArray(2));
					int[] sizeArray = new int[2];
					for (int i = 0; i < sizeArray.length; i++) {
						sizeArray[i] = GsonHelper.convertToInt(sizeArrayJson.get(i), "size[" + i + "]");
					}

					JsonArray borderArrayJson = GsonHelper.getAsJsonArray(innerObject, "border", new JsonArray(4));
					int[] borderArray = new int[4];
					for (int i = 0; i < borderArray.length; i++) {
						borderArray[i] = GsonHelper.convertToInt(borderArrayJson.get(i), "border[" + i + "]");
					}
					
					boolean tiledContent = GsonHelper.getAsBoolean(innerObject, "tiled_content", false);
					boolean tiledBorder = GsonHelper.getAsBoolean(innerObject, "tiled_border", false);
					sprite = new GLGuiTextureData.NineSlicedSprite(uvArray, sizeArray, borderArray, tiledBorder, tiledContent);
				}
			};
			result.put(key, sprite);
		}
		return new GLGuiTextureData(result, textureSize);
	}

	@Deprecated(forRemoval = true)
	@Override
	public String getMetadataSectionName() {
		return "dragonlib-gui";
	}
}
