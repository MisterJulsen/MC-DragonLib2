package de.mrjulsen.mcdragonlib.compat;

import dev.architectury.event.events.client.ClientPlayerEvent;

public final class CompatManager {

    @SuppressWarnings("unused")
    private static record Mod(String name, String id, String url) {}

    private CompatManager() {}

    public static void run() {
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((player) -> {
        });
    }
}
