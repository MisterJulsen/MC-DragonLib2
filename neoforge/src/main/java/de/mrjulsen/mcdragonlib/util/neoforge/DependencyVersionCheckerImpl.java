package de.mrjulsen.mcdragonlib.util.neoforge;

import de.mrjulsen.mcdragonlib.util.DependencyVersionChecker;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
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
