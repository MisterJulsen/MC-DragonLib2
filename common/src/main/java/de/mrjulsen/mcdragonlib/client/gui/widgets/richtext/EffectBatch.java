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

package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

import java.util.List;

public class EffectBatch {
    private final List<BakedGlyph.Effect> effects;
    private final float x;
    private final float y;
    private final float scale;

    public EffectBatch(float x, float y, float scale) {
        this.effects = Lists.newLinkedList();
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public void addEffectToBatch(BakedGlyph.Effect effect) {
        this.effects.add(effect);
    }

    public List<BakedGlyph.Effect> getEffects() {
        return effects;
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float scale() {
        return scale;
    }
}
