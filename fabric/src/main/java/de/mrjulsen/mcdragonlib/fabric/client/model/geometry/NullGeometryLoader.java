package de.mrjulsen.mcdragonlib.fabric.client.model.geometry;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class NullGeometryLoader implements IGeometryLoader<EmptyModel> {
	public static final NullGeometryLoader INSTANCE = new NullGeometryLoader();

	@Override
	public EmptyModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
		return null;
	}
}
