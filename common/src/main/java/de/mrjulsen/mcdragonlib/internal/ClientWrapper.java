package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class ClientWrapper {
    public static void openTestScreen() {
        DLScreen.setScreen(new TestScreen(TextUtils.text("TestScreen")));
    }

    @SuppressWarnings("resource")
    public static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
