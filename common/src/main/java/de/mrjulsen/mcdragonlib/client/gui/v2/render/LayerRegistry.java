/*
 * DragonLib 3 - 1.21.1
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

package de.mrjulsen.mcdragonlib.client.gui.v2.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The known render layers. Everything drawn through DLGuiGraphics#defer lands on one of them and is
 * flushed at the end of the frame, in layer order.
 * <p>That is how a tooltip or a dropdown gets out of its component's clip rectangle and on top of
 * the rest of the GUI.</p>
 */
public final class LayerRegistry {

    private static final Map<String, GuiLayer> LAYERS = new LinkedHashMap<>();
    private static List<GuiLayer> sorted = List.of();

    /**
     * Normal content, i.e. the layer for deferred drawing that should still sit below everything else.
     */
    public static final GuiLayer CONTENT = register("content", 0);
    public static final GuiLayer POPUP = register("popup", 1000);
    public static final GuiLayer DRAG = register("drag", 2000);
    public static final GuiLayer TOOLTIP = register("tooltip", 3000);

    private LayerRegistry() {
    }

    /**
     * Registers a layer, or returns the existing one if that name is already taken, so the order of an
     * existing layer is never changed.
     *
     * @param order higher means further to the front; see the constants above for the built in values
     */
    public static GuiLayer register(String name, int order) {
        GuiLayer existing = LAYERS.get(name);
        if (existing != null) {
            return existing;
        }
        GuiLayer layer = new GuiLayer(name, order);
        LAYERS.put(name, layer);
        List<GuiLayer> next = new ArrayList<>(LAYERS.values());
        Collections.sort(next);
        sorted = List.copyOf(next);
        return layer;
    }

    /**
     * The layer registered under this name, or null.
     */
    public static GuiLayer get(String name) {
        return LAYERS.get(name);
    }

    /**
     * All layers, sorted back to front.
     */
    public static List<GuiLayer> layers() {
        return sorted;
    }
}
