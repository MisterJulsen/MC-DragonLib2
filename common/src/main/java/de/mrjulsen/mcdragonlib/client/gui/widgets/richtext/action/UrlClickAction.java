/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
