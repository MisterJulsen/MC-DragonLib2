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

package de.mrjulsen.mcdragonlib.fabric.client.model.geometry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface RegisterGeometryLoadersCallback {
	Event<RegisterGeometryLoadersCallback> EVENT = EventFactory.createArrayBacked(RegisterGeometryLoadersCallback.class, callbacks -> loaders -> {
		for (RegisterGeometryLoadersCallback e : callbacks)
			e.registerGeometryLoaders(loaders);
	});

	void registerGeometryLoaders(Map<ResourceLocation, IGeometryLoader<?>> loaders);
}
