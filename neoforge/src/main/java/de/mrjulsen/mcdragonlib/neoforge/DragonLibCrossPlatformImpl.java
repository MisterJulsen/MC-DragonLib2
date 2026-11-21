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

package de.mrjulsen.mcdragonlib.neoforge;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModClientConfig;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.config.ModServerConfig;
import net.neoforged.fml.config.ModConfig;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {
        DragonLibNeoForge.getModContainer().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
        // Registered as COMMON on purpose: NeoForge only loads SERVER configs together with a world, but the values are read earlier.
        DragonLibNeoForge.getModContainer().registerConfig(ModConfig.Type.COMMON, ModServerConfig.SPEC, DragonLib.MODID + "-server.toml");
        DragonLibNeoForge.getModContainer().registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC, DragonLib.MODID + "-client.toml");
    }
}
