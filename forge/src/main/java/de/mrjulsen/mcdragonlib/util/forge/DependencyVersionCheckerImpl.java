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

package de.mrjulsen.mcdragonlib.util.forge;

import de.mrjulsen.mcdragonlib.util.DependencyVersionChecker;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyVersionCheckerImpl {

    private static final Map<DependencyVersionChecker.DependencyKey, Optional<DependencyVersionChecker.VersionResult>> versionsByMod = new ConcurrentHashMap<>();

    public static Optional<DependencyVersionChecker.VersionResult> checkDependencies(String targetModid, String dependencyModid, String versionString) {
        DependencyVersionChecker.DependencyKey key = new  DependencyVersionChecker.DependencyKey(targetModid, dependencyModid, versionString);
        return versionsByMod.computeIfAbsent(key, k -> {
            DefaultArtifactVersion targetVersion = new DefaultArtifactVersion(k.version());
            for (IModInfo mod : ModList.get().getMods()) {
                if (k.mod() != null && !mod.getModId().equals(k.mod())) {
                    continue;
                }

                for (IModInfo.ModVersion dep : mod.getDependencies()) {

                    if (!dep.getModId().equals(k.dependency())) continue;
                    VersionRange range = dep.getVersionRange();
                    ArtifactVersion minVersion = getMinimumVersion(range);

                    int cmp = minVersion.compareTo(targetVersion);

                    DependencyVersionChecker.VersionRelation relation;
                    if (cmp < 0) relation = DependencyVersionChecker.VersionRelation.IS_OLDER;
                    else if (cmp == 0) relation = DependencyVersionChecker.VersionRelation.IS_SAME;
                    else relation = DependencyVersionChecker.VersionRelation.IS_NEWER;

                    return Optional.of(new DependencyVersionChecker.VersionResult(k.mod(), k.dependency(), k.version(), minVersion.toString(), relation));
                }
                break;
            }
            return Optional.empty();
        });
    }

    private static ArtifactVersion getMinimumVersion(VersionRange range) {
        List<Restriction> restrictions = range.getRestrictions();
        if (restrictions == null || restrictions.isEmpty()) {
            return null;
        }

        Restriction first = restrictions.get(0);
        return first.getLowerBound();
    }
}
