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

import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BitflagProperty;

/**
 * Snapshot of how a component sat inside its parent's content area, taken before a resize so the
 * anchor flags can be resolved against the original gaps and the original relative center.
 * Internal helper of {@link GuiComponent#applyAnchor(Rectangle, Size)}.
 */
record AnchorFrame(Axis horizontal, Axis vertical) {

    static AnchorFrame capture(Rectangle bounds, Rectangle area) {
        return new AnchorFrame(
            new Axis(bounds.x() - area.x(), bounds.width(), area.width()),
            new Axis(bounds.y() - area.y(), bounds.height(), area.height())
        );
    }

    Rectangle resolve(BitflagProperty<Anchor> anchor, Rectangle area, Size measured) {
        Span x = horizontal.resolve(area.width(), measured.w(), anchor.has(Anchor.LEFT), anchor.has(Anchor.RIGHT));
        Span y = vertical.resolve(area.height(), measured.h(), anchor.has(Anchor.TOP), anchor.has(Anchor.BOTTOM));
        return Rectangle.withSize(area.x() + x.start(), area.y() + y.start(), x.length(), y.length());
    }

    record Span(double start, double length) {
    }

    record Axis(double start, double length, double reference) {

        Span resolve(double available, double measured, boolean anchorStart, boolean anchorEnd) {
            if (anchorStart && anchorEnd) {
                return new Span(start, Math.max(0, available - start - endGap()));
            }
            if (anchorStart) {
                return new Span(start, measured);
            }
            if (anchorEnd) {
                return new Span(available - endGap() - measured, measured);
            }
            if (reference <= 0) {
                return new Span(start, measured);
            }
            return new Span(centerRatio() * available - measured / 2D, measured);
        }

        private double endGap() {
            return reference - (start + length);
        }

        private double centerRatio() {
            return Math.max(0, (start + length / 2D) / reference);
        }
    }
}
