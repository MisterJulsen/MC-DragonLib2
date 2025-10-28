package de.mrjulsen.mcdragonlib.fabric.client.model.geometry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Allows users to register their own {@link IGeometryLoader geometry loaders} for use in block/item models.
 */
public interface RegisterGeometryLoadersCallback {
	Event<RegisterGeometryLoadersCallback> EVENT = EventFactory.createArrayBacked(RegisterGeometryLoadersCallback.class, callbacks -> loaders -> {
		for (RegisterGeometryLoadersCallback e : callbacks)
			e.registerGeometryLoaders(loaders);
	});

	void registerGeometryLoaders(Map<ResourceLocation, IGeometryLoader<?>> loaders);
}
