package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLScreenWrapper;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.network.NetworkDirection.C2S;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class ClientWrapper {
    public static void openTestScreen() {
        Minecraft.getInstance().setScreen(new DLScreenWrapper(root -> new DLWindow(root)));
    }

    @SuppressWarnings("resource")
    public static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    public static C2S toServer() {
        return packet -> {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(packet);
            } else {
                throw new IllegalStateException("Unable to send packet to the server while not in game!");
            }
        };
    }
}
