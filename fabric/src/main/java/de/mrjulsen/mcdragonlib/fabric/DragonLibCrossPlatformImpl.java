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

package de.mrjulsen.mcdragonlib.fabric;

import fuzs.forgeconfigapiport.fabric.impl.core.NeoForgeConfigRegistryImpl;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModClientConfig;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.config.ModServerConfig;
import net.neoforged.fml.config.ModConfig;

public class DragonLibCrossPlatformImpl {

    public static void registerConfig() {
        NeoForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, DragonLib.MODID + "-common.toml");
        NeoForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.SERVER, ModServerConfig.SPEC, DragonLib.MODID + "-server.toml");
        NeoForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.CLIENT, ModClientConfig.SPEC, DragonLib.MODID + "-client.toml");
    }
}
