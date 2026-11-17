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
