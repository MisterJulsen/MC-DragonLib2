package de.mrjulsen.mcdragonlib.client.atlas;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.ScaleType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public class DLGuiTextureDataSerializer implements MetadataSectionSerializer<DLTextureSheetData> {
	@Override
	public DLTextureSheetData fromJson(JsonObject json) {
		Map<String, DLTextureSheetData.AbstractSprite> result = new HashMap<>();
		
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
			
			AbstractSprite sprite = DLTextureSheetData.EMPTY_SPRITE;
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
					sprite = new DLTextureSheetData.StretchedSprite(uvArray, sizeArray);
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
					sprite = new DLTextureSheetData.TiledSprite(uvArray, sizeArray);
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
					sprite = new DLTextureSheetData.NineSlicedSprite(uvArray, sizeArray, borderArray, tiledBorder, tiledContent);
				}
			};
			result.put(key, sprite);
		}
		return new DLTextureSheetData(result, textureSize);
	}

	@Override
	public String getMetadataSectionName() {
		return "dragonlib-gui";
	}
}
