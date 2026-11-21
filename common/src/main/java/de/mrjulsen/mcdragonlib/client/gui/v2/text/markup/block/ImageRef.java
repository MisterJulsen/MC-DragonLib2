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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block;

import net.minecraft.resources.ResourceLocation;

public record ImageRef(ResourceLocation texture, int width, int height) {

    public static final int DEFAULT_SIZE = 16;
    public static final int MAX_SIZE = 512;

    public static ImageRef parse(String target) {
        if (target == null) {
            return null;
        }
        String path = target.trim();
        if (path.contains("://")) {
            return null;
        }
        int width = DEFAULT_SIZE;
        int height = DEFAULT_SIZE;

        int space = path.lastIndexOf(' ');
        if (space > 0) {
            int[] size = parseSize(path.substring(space + 1));
            if (size != null) {
                width = size[0];
                height = size[1];
                path = path.substring(0, space).trim();
            }
        }

        ResourceLocation texture = ResourceLocation.tryParse(path);
        return texture == null ? null : new ImageRef(texture, width, height);
    }

    private static int[] parseSize(String text) {
        int x = text.indexOf('x');
        if (x <= 0 || x == text.length() - 1) {
            return null;
        }
        try {
            int width = Integer.parseInt(text.substring(0, x));
            int height = Integer.parseInt(text.substring(x + 1));
            if (width <= 0 || height <= 0) {
                return null;
            }
            return new int[] { Math.min(width, MAX_SIZE), Math.min(height, MAX_SIZE) };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
