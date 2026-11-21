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

package de.mrjulsen.mcdragonlib.client.gui.v2;

/**
 * Generic alignment of a content block inside a larger space, used by layouts and text rendering.
 * <p>{@link #STRETCH} has no offset of its own; layouts interpret it as "fill the cross axis".</p>
 */
public enum Alignment {
    START,
    CENTER,
    END,
    STRETCH;

    /**
     * Offset at which a content block of the given length has to be placed to be aligned this way.
     *
     * @param available length of the space to align in
     * @param content length of the content to align
     * @return the offset from the start of the space; always 0 for START and STRETCH
     */
    public double offset(double available, double content) {
        return switch (this) {
            case CENTER -> (available - content) / 2.0D;
            case END -> available - content;
            default -> 0.0D;
        };
    }
}
