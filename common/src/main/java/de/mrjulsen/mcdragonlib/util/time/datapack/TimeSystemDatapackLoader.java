package de.mrjulsen.mcdragonlib.util.time.datapack;

import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.util.DLUtils;
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
            ResourceLocation key = DLUtils.resourceLocation(DragonLib.MODID, "time_system");
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
