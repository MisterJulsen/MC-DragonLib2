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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.input;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.document.TextRange;

public final class Caret {

    private int offset;
    private int anchor;

    private float goalX = -1.0F;

    public int offset() {
        return offset;
    }

    public int anchor() {
        return anchor;
    }

    public TextRange selection() {
        return TextRange.of(anchor, offset);
    }

    public boolean hasSelection() {
        return anchor != offset;
    }

    public void moveTo(int offset, boolean extend) {
        this.offset = offset;
        if (!extend) {
            this.anchor = offset;
        }
        this.goalX = -1.0F;
    }

    public void moveVertically(int offset, boolean extend, float goalX) {
        this.offset = offset;
        if (!extend) {
            this.anchor = offset;
        }
        this.goalX = goalX;
    }

    public void select(TextRange range) {
        this.anchor = range.start();
        this.offset = range.end();
        this.goalX = -1.0F;
    }

    public void clearSelection() {
        this.anchor = offset;
    }

    public float goalX() {
        return goalX;
    }

    public void setGoalX(float goalX) {
        this.goalX = goalX;
    }

    public void clamp(int length) {
        this.offset = Math.max(0, Math.min(offset, length));
        this.anchor = Math.max(0, Math.min(anchor, length));
    }
}
