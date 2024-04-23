package de.mrjulsen.mcdragonlib.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class DelayedTask {

    private static final Map<UUID, Consumer<IInstanceData>> instances = new HashMap<>();

    public static UUID create(Consumer<IInstanceData> consumer) {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (instances.containsKey(id));

        instances.put(id, consumer);
        return id;
    }

    public static <T extends IInstanceData> void run(UUID id, T data) {
        if (instances.containsKey(id)) {
            instances.get(id).accept(data);
        }
    }

    public static interface IInstanceData {}
    public static final class EmptyInstanceData implements IInstanceData {}
}
