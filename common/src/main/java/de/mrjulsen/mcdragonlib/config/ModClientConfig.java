package de.mrjulsen.mcdragonlib.config;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<ETextRenderingMode> SMOOTH_TEXT;

    static {
        BUILDER.push(DragonLib.MODID + "_client_config");
        SMOOTH_TEXT = BUILDER.comment("EXPERIMENTAL: When enabled, text is rendered with smooth edges, which improves readability with small font sizes or at far distances and reduces visual noise. PLEASE NOTE: Turn this option OFF, if it is causing issues with text rendering. (Default: AUTO, AUTO = Disabled when using shader packs)")
            .defineEnum("experimental.smooth_text", ETextRenderingMode.AUTO);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static ETextRenderingMode getSmoothTextModeSafe() {
        try {
            return SMOOTH_TEXT.get();
        } catch (Throwable e) {
            return ETextRenderingMode.AUTO;
        }
    }
}
