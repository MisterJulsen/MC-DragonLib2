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

package de.mrjulsen.mcdragonlib.client.gui.v2.behaviour;

import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;

/**
 * A piece of reusable behaviour that can be attached to any component, instead of baking it into a
 * subclass. Moving, resizing, scrolling, tooltips and hotkeys all work this way.
 * <p>Only one behaviour per class can be attached to a component; attaching another one replaces it.
 * Implementations should extend {@link AbstractBehaviour}, which already handles the host reference
 * and cleans up event listeners.</p>
 */
public interface Behaviour {
    /**
     * Called when this behaviour was attached to a component.
     */
    void onAttach(GuiComponent host);
    /**
     * Called when it was removed again, also when the component is disposed of.
     */
    void onDetach(GuiComponent host);
}
