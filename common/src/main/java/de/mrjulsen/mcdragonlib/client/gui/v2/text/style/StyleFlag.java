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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.style;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.ChatFormatting;

public final class StyleFlag {

    private static final int MAX_BITS = 32;

    private static final Map<String, StyleFlag> REGISTRY = new LinkedHashMap<>();

    private static int allocatedBits;

    public static final StyleFlag MARKUP = register("markup");

    private final String name;
    private final int bit;
    private final IStyleEffect effect;

    private StyleFlag(String name, int bit, IStyleEffect effect) {
        this.name = name;
        this.bit = bit;
        this.effect = effect;
    }

    public static StyleFlag register(String name) {
        return register(name, IStyleEffect.NONE);
    }

    public static StyleFlag register(String name, IStyleEffect effect) {
        if (REGISTRY.containsKey(name)) {
            throw new IllegalArgumentException("A style flag named '" + name + "' is already registered.");
        }
        if (allocatedBits >= MAX_BITS) {
            throw new IllegalStateException("No style flag bits left to allocate.");
        }
        StyleFlag flag = new StyleFlag(name, 1 << allocatedBits++, effect == null ? IStyleEffect.NONE : effect);
        REGISTRY.put(name, flag);
        return flag;
    }

    public static StyleFlag byName(String name) {
        return REGISTRY.get(name);
    }

    public static StyleFlag byFormatting(ChatFormatting formatting) {
        if (formatting == null) {
            return null;
        }
        for (StyleFlag flag : REGISTRY.values()) {
            if (flag.effect.formatting() == formatting) {
                return flag;
            }
        }
        return null;
    }

    public static Collection<StyleFlag> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public String name() {
        return name;
    }

    public int bit() {
        return bit;
    }

    public IStyleEffect effect() {
        return effect;
    }

    public boolean isSet(int flags) {
        return (flags & bit) != 0;
    }

    public int set(int flags) {
        return flags | bit;
    }

    public int clear(int flags) {
        return flags & ~bit;
    }

    public int setIf(int flags, boolean value) {
        return value ? set(flags) : clear(flags);
    }

    @Override
    public String toString() {
        return name;
    }
}
