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

package de.mrjulsen.mcdragonlib.client.gui.v2;

import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

/**
 * Global switches for the GUI debug overlays. Purely visual, nothing here affects layout.
 */
public final class GuiDebug {

    /**
     * Draws the clip rectangle of every component that clips.
     */
    public static final BooleanProperty showClipBounds = new BooleanProperty(false);
    /**
     * Stops clipping altogether, so content that would be cut off stays visible.
     */
    public static final BooleanProperty disableClipping = new BooleanProperty(false);
    /**
     * Outlines every rendered component, and culled ones in {@link #culledBoundsColor}.
     */
    public static final BooleanProperty showComponentBounds = new BooleanProperty(false);
    public static final Property<DLColor> clipBoundsColor = new Property<>(DLColor.of(255, 255, 80, 0));
    public static final Property<DLColor> componentBoundsColor = new Property<>(DLColor.of(120, 80, 200, 255));
    public static final Property<DLColor> culledBoundsColor = new Property<>(DLColor.of(160, 255, 0, 0));

    private GuiDebug() {
    }

    /**
     * Whether anything clip related is currently being visualized.
     */
    public static boolean isClipOverlayActive() {
        return showClipBounds.get() || disableClipping.get();
    }

    /**
     * Flips all three overlays at once, based on the current state of {@link #showClipBounds}.
     */
    public static void toggleCullingOverlay() {
        boolean next = !showClipBounds.get();
        showClipBounds.set(next);
        disableClipping.set(next);
        showComponentBounds.set(next);
    }
}
