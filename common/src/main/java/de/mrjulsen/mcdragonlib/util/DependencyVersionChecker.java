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

package de.mrjulsen.mcdragonlib.util;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.Optional;

public class DependencyVersionChecker {

    public enum VersionRelation {
        IS_OLDER,
        IS_SAME,
        IS_NEWER;
    }

    public record VersionResult(String modid, String dependencyModId, String requestedVersionString, String requiredVersionString, VersionRelation relation) {}

    public record DependencyKey(String mod, String dependency, String version) {}

    @ExpectPlatform
    public static Optional<VersionResult> checkDependencies(String targetModid, String dependencyModid, String versionString) {
        throw new AssertionError();
    }
}
