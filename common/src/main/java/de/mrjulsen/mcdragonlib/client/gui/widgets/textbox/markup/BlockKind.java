package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BlockKind {

    public static final int TRAIT_NONE = 0;
    public static final int TRAIT_INDENTABLE = 1;
    public static final int TRAIT_DECORATION = 1 << 1;
    public static final int TRAIT_MARKER = 1 << 2;
    public static final int TRAIT_NO_ALIGNMENT = 1 << 3;
    public static final int TRAIT_HANGING_INDENT = 1 << 4;

    private static final Map<String, BlockKind> REGISTRY = new LinkedHashMap<>();

    public static final BlockKind PARAGRAPH = register("paragraph", TRAIT_NONE);

    private final String name;
    private final int traits;

    private BlockKind(String name, int traits) {
        this.name = name;
        this.traits = traits;
    }

    public static BlockKind register(String name, int traits) {
        BlockKind existing = REGISTRY.get(name);
        if (existing != null) {
            throw new IllegalArgumentException("A block kind named '" + name + "' is already registered.");
        }
        BlockKind kind = new BlockKind(name, traits);
        REGISTRY.put(name, kind);
        return kind;
    }

    public static BlockKind byName(String name) {
        return REGISTRY.get(name);
    }

    public static Collection<BlockKind> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public String name() {
        return name;
    }

    public int traits() {
        return traits;
    }

    public boolean has(int trait) {
        return (traits & trait) != 0;
    }

    public boolean isIndentable() {
        return has(TRAIT_INDENTABLE);
    }

    public boolean hasHangingIndent() {
        return has(TRAIT_HANGING_INDENT);
    }

    public boolean isDecoration() {
        return has(TRAIT_DECORATION);
    }

    public boolean reservesMarkerSpace() {
        return has(TRAIT_MARKER);
    }

    public boolean allowsAlignment() {
        return !has(TRAIT_NO_ALIGNMENT);
    }

    @Override
    public String toString() {
        return name;
    }
}
