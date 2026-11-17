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

package de.mrjulsen.mcdragonlib.util.fabric;

import de.mrjulsen.mcdragonlib.util.DependencyVersionChecker;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.metadata.ModDependency;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyVersionCheckerImpl {

    private static final Map<DependencyVersionChecker.DependencyKey, Optional<DependencyVersionChecker.VersionResult>> versionsByMod = new ConcurrentHashMap<>();

    public static Optional<DependencyVersionChecker.VersionResult> checkDependencies(String targetModid, String dependencyModid, String versionString) {
        DependencyVersionChecker.DependencyKey key = new  DependencyVersionChecker.DependencyKey(targetModid, dependencyModid, versionString);
        return versionsByMod.computeIfAbsent(key, k -> {
            SemanticVersion targetVersion;

            try {
                targetVersion = SemanticVersion.parse(k.version());
            } catch (VersionParsingException e) {
                return Optional.empty();
            }

            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                String modId = mod.getMetadata().getId();
                if (k.mod() != null && !modId.equals(k.mod())) {
                    continue;
                }

                for (ModDependency dep : mod.getMetadata().getDependencies()) {
                    if (!dep.getModId().equals(k.dependency())) continue;
                    String rawRequirement = dep.getVersionRequirements().toString();
                    SemanticVersion minVersion = extractMinimumFabricVersion(rawRequirement);

                    if (minVersion == null) {
                        continue;
                    }

                    int cmp = minVersion.compareTo(targetVersion);

                    DependencyVersionChecker.VersionRelation relation;
                    if (cmp < 0) relation = DependencyVersionChecker.VersionRelation.IS_OLDER;
                    else if (cmp == 0) relation = DependencyVersionChecker.VersionRelation.IS_SAME;
                    else relation = DependencyVersionChecker.VersionRelation.IS_NEWER;

                    return Optional.of(new DependencyVersionChecker.VersionResult(k.mod(), k.dependency(), k.version(), rawRequirement, relation));
                }
                break;
            }
            return Optional.empty();
        });
    }

    private static SemanticVersion extractMinimumFabricVersion(String req) {
        try {
            if (req.contains(">=")) {
                String versionPart = req.replace(">=", "").replace("[", "").replace("]", "").trim();
                return SemanticVersion.parse(versionPart);
            }
        } catch (VersionParsingException ignored) {
        }

        return null;
    }
}
