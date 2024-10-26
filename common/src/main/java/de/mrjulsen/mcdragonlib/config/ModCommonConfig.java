package de.mrjulsen.mcdragonlib.config;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> TICKS_PER_DAY;
    public static final ForgeConfigSpec.ConfigValue<Double> TIME_MULTIPLIER;

    static {
        BUILDER.push(DragonLib.MODID + "_common_config");
        
        TICKS_PER_DAY = BUILDER.comment("The number of ticks for one Minecraft day. If in doubt, leave it unchanged! (Default: 24000)")
            .defineInRange("time.ticks_per_day", 24000, 0, Integer.MAX_VALUE);
        TIME_MULTIPLIER = BUILDER.comment("The scale of the tick length. 1 means that a tick has normal duration (0.05 seconds). 20 would therefore result in a length of 1 second per tick. If in doubt, leave it unchanged! (Default: 1)")
            .defineInRange("time.time_multiplier", 1D, 0D, Double.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

