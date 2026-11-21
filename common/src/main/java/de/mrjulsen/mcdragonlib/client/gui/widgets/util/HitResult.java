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

package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeMap;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;

public class HitResult {
    
    public record ComponentHitContext(DLGuiComponent component, double mouseX, double mouseY, int xOffset, int yOffset, boolean enabled) {}

    public enum ComponentSelectionState {
        UNSELECTED,
        HIT,
        FOCUSED;

        public boolean isHit() {
            return this == HIT || this == FOCUSED;
        }

        public boolean isNotFocused() {
            return this == HIT || this == UNSELECTED;
        }
    }


    private TreeMap<ComponentSelectionState, LinkedList<ComponentHitContext>> allHitComponents = new TreeMap<>();
    private boolean consumed;

    public HitResult() {
        allHitComponents = new TreeMap<>();
    }

    public void add(ComponentSelectionState pain, ComponentHitContext context) {
        allHitComponents.computeIfAbsent(pain, x -> new LinkedList<>()).add(context);
    }

    public void addAll(ComponentSelectionState pain, Collection<ComponentHitContext> context) {
        allHitComponents.computeIfAbsent(pain, x -> new LinkedList<>()).addAll(context);
    }

    public void addAll(TreeMap<ComponentSelectionState, LinkedList<ComponentHitContext>> entries) {
        entries.entrySet().forEach((a) -> addAll(a.getKey(), a.getValue()));
    }
    
    public boolean isPresent() {
        return !allHitComponents.isEmpty() && (allHitComponents.containsKey(ComponentSelectionState.FOCUSED) || allHitComponents.containsKey(ComponentSelectionState.HIT));
    }

    public Optional<ComponentHitContext> focusedComponent() {
        return Optional.ofNullable(isPresent() && components().containsKey(ComponentSelectionState.FOCUSED) ? components().get(ComponentSelectionState.FOCUSED).get(0) : null);
    }

    public boolean consumed() {
        return consumed;
    }

    public TreeMap<ComponentSelectionState, LinkedList<ComponentHitContext>> components() {
        return allHitComponents;
    }

    public void consume(boolean b) {
        this.consumed = b;
    }
}