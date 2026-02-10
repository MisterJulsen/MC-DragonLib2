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
