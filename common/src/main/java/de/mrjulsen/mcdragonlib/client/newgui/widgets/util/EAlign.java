package de.mrjulsen.mcdragonlib.client.newgui.widgets.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

public enum EAlign implements BitflagEnum {
    TOP("top", 0b0001, AffectedTransformtion.POSITION),
    LEFT("left", 0b0010, AffectedTransformtion.POSITION),
    RIGHT("right", 0b0100, AffectedTransformtion.SIZE),
    BOTTOM("bottom", 0b1000, AffectedTransformtion.SIZE);

    private final String name;
    private final int bit;
    private final AffectedTransformtion transform;

    private static final ImmutableMap<Long, EAlign> valueMap;
    static {
        Map<Long, EAlign> map = new HashMap<>();
        for (EAlign value : values()) {
            map.put(value.getBit(), value);
        }
        valueMap = ImmutableMap.copyOf(map);
    }

    private EAlign(String name, int bit, AffectedTransformtion transform) {
        this.name = name;
        this.bit = bit;
        this.transform = transform;
    }

    public String getName() {
        return name;
    }

    public long getBit() {
        return bit;
    }

    public AffectedTransformtion getAffectedTransformation() {
        return transform;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Optional<EAlign> getByBit(long bit) {
        return Optional.ofNullable(valueMap.containsKey(bit) ? valueMap.get(bit) : null);
    }

    public static enum AffectedTransformtion {
        NONE,
        POSITION,
        SIZE;
    }
}
