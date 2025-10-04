package de.mrjulsen.mcdragonlib.client.atlas;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.ScaleType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public class GLGuiTextureDataSerializer implements MetadataSectionSerializer<GLGuiTextureData> {
	@Override
	public GLGuiTextureData fromJson(JsonObject json) {
		Map<String, GLGuiTextureData.AbstractSprite> result = new HashMap<>();
		
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
					JsonArray uvArrayJson = GsonHelper.getAsJsonArray(innerObject, "uv");
					int[] uvArray = new int[2];
					for (int i = 0; i < uvArray.length; i++) {
						uvArray[i] = GsonHelper.convertToInt(uvArrayJson.get(i), "uv[" + i + "]");
					}

					JsonArray sizeArrayJson = GsonHelper.getAsJsonArray(innerObject, "size");
					int[] sizeArray = new int[2];
					for (int i = 0; i < sizeArray.length; i++) {
						sizeArray[i] = GsonHelper.convertToInt(sizeArrayJson.get(i), "size[" + i + "]");
					}
					sprite = new GLGuiTextureData.StretchedSprite(uvArray, sizeArray);
				}
				case TILE -> {
					JsonArray uvArrayJson = GsonHelper.getAsJsonArray(innerObject, "uv");
					int[] uvArray = new int[2];
					for (int i = 0; i < uvArray.length; i++) {
						uvArray[i] = GsonHelper.convertToInt(uvArrayJson.get(i), "uv[" + i + "]");
					}

					JsonArray sizeArrayJson = GsonHelper.getAsJsonArray(innerObject, "size");
					int[] sizeArray = new int[2];
					for (int i = 0; i < sizeArray.length; i++) {
						sizeArray[i] = GsonHelper.convertToInt(sizeArrayJson.get(i), "size[" + i + "]");
					}
					sprite = new GLGuiTextureData.TiledSprite(uvArray, sizeArray);
				}
				case NINE_SLICE -> {
					JsonArray uvArrayJson = GsonHelper.getAsJsonArray(innerObject, "uv");
					int[] uvArray = new int[2];
					for (int i = 0; i < uvArray.length; i++) {
						uvArray[i] = GsonHelper.convertToInt(uvArrayJson.get(i), "uv[" + i + "]");
					}

					JsonArray sizeArrayJson = GsonHelper.getAsJsonArray(innerObject, "size");
					int[] sizeArray = new int[2];
					for (int i = 0; i < sizeArray.length; i++) {
						sizeArray[i] = GsonHelper.convertToInt(sizeArrayJson.get(i), "size[" + i + "]");
					}

					JsonArray borderArrayJson = GsonHelper.getAsJsonArray(innerObject, "border");
					int[] borderArray = new int[4];
					for (int i = 0; i < borderArray.length; i++) {
						borderArray[i] = GsonHelper.convertToInt(borderArrayJson.get(i), "border[" + i + "]");
					}
					sprite = new GLGuiTextureData.NineSlicedSprite(uvArray, sizeArray, borderArray);
				}
			};
			result.put(key, sprite);
		}
		return new GLGuiTextureData(result, textureSize);
	}

	@Override
	public String getMetadataSectionName() {
		return "dragonlib-gui";
	}
}
