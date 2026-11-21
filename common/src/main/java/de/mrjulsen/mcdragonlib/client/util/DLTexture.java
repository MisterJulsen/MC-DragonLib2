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

package de.mrjulsen.mcdragonlib.client.util;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public class DLTexture {
    private final ResourceLocation texture;
    private final int textureId;
    private final boolean useTextureId;
    private final int textureWidth;
    private final int textureHeight;

    @Deprecated
    public DLTexture(ResourceLocation texture, int textureId, boolean useTextureId, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.textureId = textureId;
        this.useTextureId = useTextureId;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public DLTexture(ResourceLocation texture, int width, int height) {
        this(texture, 0, false, width, height);
    }

    public DLTexture(int textureId, int width, int height) {
        this(null, textureId, true, width, height);
    }

    public Optional<ResourceLocation> getTexture() {
        return Optional.ofNullable(texture);
    }

    public int getTextureId() {
        return textureId;
    }

    public boolean usesTextureId() {
        return useTextureId;
    }

    public int width() {
        return textureWidth;
    }

    public int height() {
        return textureHeight;
    }
}
