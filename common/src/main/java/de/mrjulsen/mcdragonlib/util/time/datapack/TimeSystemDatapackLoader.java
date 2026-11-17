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

package de.mrjulsen.mcdragonlib.util.time.datapack;

import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.util.time.DatapackTimeSystem;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class TimeSystemDatapackLoader extends SimpleJsonResourceReloadListener {

    public static final String ID = "config";
    public static TimeSystemDatapackLoader instance;
    private DatapackTimeSystem system;

    public TimeSystemDatapackLoader() {
        super(new Gson(), ID);
        instance = this;
    }

    public static TimeSystemDatapackLoader getInstance() {
        return instance;
    }

    public static boolean hasTimeSystem() {
        return instance != null && instance.system != null;
    }

    public static Optional<ITimeSystem> getTimeSystem() {
        return Optional.ofNullable(hasTimeSystem() ? instance.system : null);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        try {
            ResourceLocation key = new ResourceLocation(DragonLib.MODID, "time_system");
            JsonElement json = objects.get(key);
            if (json == null) {
                system = null;
            } else {
                system = DatapackTimeSystem.fromJson(json.getAsJsonObject());
            }
        } catch (Exception e) {
            DragonLib.LOGGER.error("Failed to load time_system.json from datapack", e);
            system = null;
        }
    }
}
