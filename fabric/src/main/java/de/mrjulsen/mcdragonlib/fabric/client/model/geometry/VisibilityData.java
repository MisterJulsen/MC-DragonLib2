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

import java.util.HashMap;
import java.util.Map;

public class VisibilityData {
	private final Map<String, Boolean> data = new HashMap<>();

	public boolean hasCustomVisibility(String part) {
		return data.containsKey(part);
	}

	public boolean isVisible(String part, boolean fallback) {
		return data.getOrDefault(part, fallback);
	}

	public void setVisibilityState(String partName, boolean type) {
		data.put(partName, type);
	}

	public void copyFrom(VisibilityData visibilityData) {
		data.clear();
		data.putAll(visibilityData.data);
	}
}
