package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.action;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import java.net.URI;

public class UrlClickAction implements ClickAction, TooltipAction {
    private final String url;

    public UrlClickAction(String url) {
        this.url = url;
    }

    @Override
    public void onClick() {
        try {
            Util.getPlatform().openUri(new URI(url));
        } catch (Exception e) {
            System.err.println("Failed to open URL: " + url);
            e.printStackTrace();
        }
    }

    @Override
    public Component getTooltip() {
        return Component.literal(this.url);
    }

    public String getUrl() {
        return url;
    }
}
