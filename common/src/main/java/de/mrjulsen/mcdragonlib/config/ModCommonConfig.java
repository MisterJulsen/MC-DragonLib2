package de.mrjulsen.mcdragonlib.config;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> TICKS_PER_DAY;
    public static final ForgeConfigSpec.ConfigValue<Integer> DAYTIME_SHIFT;
    public static final ForgeConfigSpec.ConfigValue<Double> TIME_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<ECachingMode> CACHING;

    static {
        BUILDER.push(DragonLib.MODID + "_common_config");
        
        TICKS_PER_DAY = BUILDER.comment("The number of ticks for one Minecraft day. If in doubt, leave it unchanged! (Default: 24000)")
            .defineInRange("time.ticks_per_day", 24000, 0, Integer.MAX_VALUE);
        TIME_MULTIPLIER = BUILDER.comment("The scale of the tick length. 1 means that a tick has normal duration (0.05 seconds). 20 would therefore result in a length of 1 second per tick. If in doubt, leave it unchanged! (Default: 1)")
            .defineInRange("time.time_multiplier", 1D, 0D, Double.MAX_VALUE);
        DAYTIME_SHIFT = BUILDER.comment("The number of ticks by which the time of day is shifted to match the real daytime. By default, 0 ticks is 6 AM, but 0 ticks should represent 12 AM (midnight). If in doubt, leave it unchanged! (Default: 6000)")
            .defineInRange("time.daytime_shift", 6000, 0, Integer.MAX_VALUE);
        CACHING = BUILDER.comment("Specifies how aggressively data should be cached. The lower the value, the less data will be cached, which can reduce RAM usage. However, depending on the situation, less caching can lead to increased CPU usage and cause lag. Only works with mods that actively use the feature! If in doubt, leave unchanged. (Default: NORMAL, OFF = only the most important data will be cached)")
            .defineEnum("caching.mode", ECachingMode.NORMAL);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

