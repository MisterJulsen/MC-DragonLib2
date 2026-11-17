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

package de.mrjulsen.mcdragonlib.forge;

import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ComparableVersion;

@Mod(DragonLib.MODID)
public class DragonLibForge {
    public DragonLibForge() {
        EventBuses.registerModEventBus(DragonLib.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        DragonLib.init();
    }



    private static final ComparableVersion REQUIRED = new ComparableVersion("3.0.20");

    public static void scanAllMods() {

        // 1. DragonLib Version holen (falls installiert)
        ModInfo dragonLibInfo = (ModInfo) ModList.get()
                .getModContainerById("dragonlib")
                .map(c -> c.getModInfo())
                .orElse(null);

        if (dragonLibInfo == null) {
            System.out.println("DragonLib ist NICHT installiert!");
            return;
        }

        ComparableVersion installedDragonLib = new ComparableVersion(dragonLibInfo.getVersion().toString());

        // 2. Alle Mods durchgehen
        for (IModInfo mod : ModList.get().getMods()) {

            // 3. Prüfen ob diese Mod DragonLib als Dependency hat
            boolean dependsOnDragonLib = mod.getDependencies().stream().anyMatch(dep -> dep.getModId().equals("dragonlib"));

            if (!dependsOnDragonLib) continue;

            // 4. Dependency gefunden → Version check
            if (installedDragonLib.compareTo(REQUIRED) > 0) {
                System.out.println(mod.getModId()
                        + " nutzt DragonLib → Version OK ("
                        + installedDragonLib + ")");
            } else {
                System.out.println(mod.getModId()
                        + " nutzt DragonLib → Version ZU ALT! ("
                        + installedDragonLib + ")");
            }
        }
    }
}
