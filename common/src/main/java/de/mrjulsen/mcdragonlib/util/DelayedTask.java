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

package de.mrjulsen.mcdragonlib.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class DelayedTask {

    private static final Map<UUID, Consumer<IInstanceData>> instances = new HashMap<>();

    public static UUID create(Consumer<IInstanceData> consumer) {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (instances.containsKey(id));

        instances.put(id, consumer);
        return id;
    }

    public static <T extends IInstanceData> void run(UUID id, T data) {
        if (instances.containsKey(id)) {
            instances.get(id).accept(data);
        }
    }

    public static interface IInstanceData {}
    public static final class EmptyInstanceData implements IInstanceData {}
}
