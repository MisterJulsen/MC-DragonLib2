package de.mrjulsen.mcdragonlib.events.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.util.Pair;

import de.mrjulsen.mcdragonlib.events.client.ModelEvents.ModifyModels.Context;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents.ModifyModels.ModelModifier;
import de.mrjulsen.mcdragonlib.events.client.ModelEvents.ModifyModels.ModelReplacer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;

public final class ModelReplacements {
    private ModelReplacements() {}

    private final ModelModifier collector = new ModelModifier() {
        @Override
        public void replace(ModelResourceLocation location, ModelReplacer replacer) {
            isStillCollecting();
            Objects.requireNonNull(location);
            Objects.requireNonNull(replacer);
            named.merge(location, replacer, ModelReplacements::combine);
        }

        @Override
        public void replaceIf(Predicate<ModelResourceLocation> filter, ModelReplacer replacer) {
            isStillCollecting();
            Objects.requireNonNull(filter);
            Objects.requireNonNull(replacer);
            filtered.add(new Pair<>(filter, replacer));
        }
    };

    private final Map<ModelResourceLocation, ModelReplacer> named = new HashMap<>();
    private final List<Pair<Predicate<ModelResourceLocation>, ModelReplacer>> filtered = new ArrayList<>();
    private boolean collecting = true;


    public static ModelReplacements collect() {
        ModelReplacements replacements = new ModelReplacements();
        ModelEvents.MODIFY_MODELS.invoker().collectModels(replacements.collector);
        replacements.collecting = false;
        return replacements;
    }

    private void isStillCollecting() {
        if (!collecting) {
            throw new IllegalStateException("The ModelModifier is only valid during ModifyModels#collectModels.");
        }
    }

    public boolean isEmpty() {
        return named.isEmpty() && filtered.isEmpty();
    }

    public boolean hasFilters() {
        return !filtered.isEmpty();
    }

    public Set<ModelResourceLocation> namedLocations() {
        return Collections.unmodifiableSet(named.keySet());
    }

    public boolean has(ModelResourceLocation location) {
        return named.containsKey(location);
    }

    public boolean matches(ModelResourceLocation location) {
        for (Pair<Predicate<ModelResourceLocation>, ModelReplacer> pair : filtered) {
            if (pair.getFirst().test(location)) {
                return true;
            }
        }
        return false;
    }

    public boolean isModified(ModelResourceLocation location) {
        return has(location) || matches(location);
    }

    private static ModelReplacer combine(ModelReplacer a, ModelReplacer b) {
        return (originalModel, context) -> {
            BakedModel model = a.replace(originalModel, context);
            return b.replace(model == null ? originalModel : model, context);
        };
    }

    public BakedModel apply(ModelResourceLocation location, Function<ModelResourceLocation, BakedModel> lookup, Context context) {
        BakedModel current = null;
        boolean fetched = false;
        boolean changed = false;

        ModelReplacer namedReplacer = named.get(location);
        if (namedReplacer != null) {
            current = lookup.apply(location);
            fetched = true;
            BakedModel model = namedReplacer.replace(current, context);
            if (model != null) {
                current = model;
                changed = true;
            }
        }

        for (Pair<Predicate<ModelResourceLocation>, ModelReplacer> pair : filtered) {
            if (!pair.getFirst().test(location)) {
                continue;
            }

            if (!fetched) {
                current = lookup.apply(location);
                fetched = true;
            }
            BakedModel model = pair.getSecond().replace(current, context);
            if (model != null) {
                current = model;
                changed = true;
            }
        }

        return changed ? current : null;
    }
}
