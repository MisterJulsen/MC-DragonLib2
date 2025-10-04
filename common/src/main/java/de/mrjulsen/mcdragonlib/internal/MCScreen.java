package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MCScreen extends Screen {
    public MCScreen(Component title) {
        super(title);
    }

    private MultiLineEditBox box;

    @Override
    protected void init() {
        super.init();
        box = new MultiLineEditBox(Minecraft.getInstance().font, 50, 50, 200, 100, TextUtils.text("Eingabe..."), TextUtils.text("Hello World!"));
        addRenderableWidget(box);
    }

    @Override
    public void tick() {
        super.tick();
        box.tick();
    }
}
