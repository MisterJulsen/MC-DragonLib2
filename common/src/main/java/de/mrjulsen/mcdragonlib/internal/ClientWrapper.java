package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.util.TextUtils;

public class ClientWrapper {
    public static void openTestScreen() {
        DLScreen.setScreen(new TestScreen(TextUtils.text("TestScreen")));
    }
}
