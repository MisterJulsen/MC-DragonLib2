package de.mrjulsen.mcdragonlib.fabric.client.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public interface IGeometryLoader<T extends IUnbakedGeometry<T>> {
	T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException;
}
