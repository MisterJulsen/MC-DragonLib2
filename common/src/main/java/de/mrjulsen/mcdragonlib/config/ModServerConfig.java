package de.mrjulsen.mcdragonlib.config;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> USE_CUSTOM;
    public static final ForgeConfigSpec.ConfigValue<Boolean> AUTO_DETECT_TIME_SYSTEM;
    public static final ForgeConfigSpec.ConfigValue<String> PREFERRED_MOD;

    static {
        BUILDER.push(DragonLib.MODID + "_server_config");
        BUILDER.comment("These settings determine the default time system in DragonLib. Mods can use a fixed time system, meaning these settings may not always work. Datapacks can override all these settings!");

        USE_CUSTOM = BUILDER.comment(new String[] { "When ON, the custom time settings from the common config are used. When OFF, the standard vanilla time system is used. (Default: OFF)" })
            .define("time_system.use_custom", false);
        AUTO_DETECT_TIME_SYSTEM = BUILDER.comment(new String[] { "When ON, DragonLib tries to detect time changing mods (like TimeControl) and uses its settings. When OFF, Vanilla or Custom is used. (Default: ON)" })
            .define("time_system.auto_detect_time_system", true);
        PREFERRED_MOD = BUILDER.comment(new String[] { "Specifies the preferred time changing mod. If multiple such mods are installed, it cannot be clearly determined which mod DragonLib is using to retrieve the data. This option allows you to specify a particular mod. However, installing multiple such mods simultaneously is not recommended. If this value is empty, the mod will be selected automatically, as before. If the specified mod does not exist, vanilla will be used. (Default: '')" })
            .define("time_system.preferred_mod", "");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

