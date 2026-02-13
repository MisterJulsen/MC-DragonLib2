package de.mrjulsen.mcdragonlib.config;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ModCommonConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Boolean> DEBUG_NETWORKING;
    public static final ModConfigSpec.ConfigValue<ECachingMode> CACHING;
    
    public static final ModConfigSpec.ConfigValue<Boolean> TIME_AUTO;
    public static final ModConfigSpec.ConfigValue<Long> TIME_TICKS_PER_DAY;
    public static final ModConfigSpec.ConfigValue<Double> TIME_DEFAULT_TPS;
    public static final ModConfigSpec.ConfigValue<Double> TIME_DAYTIME_SHIFT_FACTOR;
    public static final ModConfigSpec.ConfigValue<Integer> TIME_SYSTEM_CACHE_TTL;
    public static final ModConfigSpec.ConfigValue<Integer> NETWORK_RESPONSE_TIMEOUT;
    public static final ModConfigSpec.ConfigValue<Integer> NETWORK_THREAD_TIMEOUT;
    public static final ModConfigSpec.ConfigValue<Integer> NETWORK_THREAD_COUNT;

    static {
        BUILDER.push(DragonLib.MODID + "_common_config");
        CACHING = BUILDER.comment("Specifies how aggressively data should be cached. The lower the value, the less data will be cached, which can reduce RAM usage. However, depending on the situation, less caching can lead to increased CPU usage and cause lag. Only works with mods that actively use the feature! If in doubt, leave unchanged. (Default: NORMAL, OFF = only the most important data will be cached)")
            .defineEnum("caching.mode", ECachingMode.NORMAL);

            
        DEBUG_NETWORKING = BUILDER.comment("Prints addidional information about the networking system in the console. For debugging purposes. (Default: OFF)")
            .define("debug.networking_logging", false);

            
        TIME_AUTO = BUILDER.comment("When enabled, DragonLib automatically adjusts the time system, depending on which time modification mod is installed and whether a compat is available for it. (Default: ON)")
            .define("time_system.auto_adjustment", true);
        TIME_TICKS_PER_DAY = BUILDER.comment(new String[] { "in Ticks", "The number of ticks per Minecraft day. This value is overridden if [Auto Adjustment] is enabled. (Default: 24000)" })
            .defineInRange("time_system.ticks_per_day", 24000L, 1L, Long.MAX_VALUE);
        TIME_DEFAULT_TPS = BUILDER.comment(new String[] { "in Ticks Per Second", "The ticks per second at which time of day advances. This value is overridden when [Auto Adjustment] is enabled or when time zones are defined. (Default: 20)" })
            .defineInRange("time_system.ticks_per_second", 20D, 0D, (double)Integer.MAX_VALUE);
        TIME_DAYTIME_SHIFT_FACTOR = BUILDER.comment("The proportion of the total day duration by which the clock time is shifted. By default, 0 ticks = 06:00, for which the time must be shifted by 25% of the day length. (Default: 0.25)")
            .defineInRange("time_system.daytime_shift", 0.25D, 0D, 1D);
        TIME_SYSTEM_CACHE_TTL = BUILDER.comment(new String[] { "in Milliseconds", "To improve performance during frequent queries of the default time system, the found time system can be cached for a certain period. Higher values may produce less accurate results because the time system is not updated immediately, for example, when a datapack is loaded. A value of 0 disables the cache. (Default: 100)", "A GAME RESTART IS REQUIRED FOR CHANGES TO TAKE EFFECT!" })
            .defineInRange("time_system.time_system_cache_ttl", 100, 0, 10000);

        NETWORK_RESPONSE_TIMEOUT = BUILDER.comment(new String[] { "in Seconds", "The time to wait for a response before an error is thrown. (Default: 60)" })
                .defineInRange("networking.response_timeout", 60, 10, 300);
        NETWORK_THREAD_TIMEOUT = BUILDER.comment(new String[] { "in Seconds", "The maximum amount of time a networking thread can be busy before the task is terminated. (Default: 30)", "A WORLD RESTART IS REQUIRED FOR CHANGES TO TAKE EFFECT!" })
                .defineInRange("networking.thread_timeout", 30, 10, 60);
        NETWORK_THREAD_COUNT = BUILDER.comment(new String[] { "The number of threads to be used for networking. This should not exceed the number of available CPU cores, as more threads will cause performance issues. By default (auto = 0), the amount of available CPU cores are used, but at least two. (Default: 0)", "A WORLD RESTART IS REQUIRED FOR CHANGES TO TAKE EFFECT!" })
                .defineInRange("networking.thread_count", 0, 0, 32);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

