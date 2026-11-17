package de.mrjulsen.mcdragonlib.client.model.extension;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;

public record DLFaceKey(int index, Direction direction) {

    public static <T> Map<DLFaceKey, T> rotate(Map<DLFaceKey, T> data, ModelState modelState) {
        Matrix4f matrix = modelState.getRotation().getMatrix();
        Map<DLFaceKey, T> rotated = new HashMap<>(data.size());
        for (Map.Entry<DLFaceKey, T> entry : data.entrySet()) {
            DLFaceKey key = entry.getKey();
            Direction direction = key.direction() == null ? null : Direction.rotate(matrix, key.direction());
            rotated.put(new DLFaceKey(key.index(), direction), entry.getValue());
        }
        return rotated;
    }
}
