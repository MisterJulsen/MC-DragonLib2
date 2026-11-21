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

package de.mrjulsen.mcdragonlib.client.model.extension;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;

public record DLFaceKey(int index, Direction direction) {

    public static <T> Map<DLFaceKey, T> rotate(Map<DLFaceKey, T> data, ModelState modelState) {
        Matrix4f matrix = modelState.getRotation().getMatrix();
        Map<DLFaceKey, T> rotated = new HashMap<>(data.size());
        for (Map.Entry<DLFaceKey, T> entry : data.entrySet()) {
            DLFaceKey key = entry.getKey();
            Direction direction = key.direction() == null ? null : Direction.rotate(matrix, key.direction());
            rotated.put(new DLFaceKey(key.index(), direction), entry.getValue());
        }
        return rotated;
    }
}
