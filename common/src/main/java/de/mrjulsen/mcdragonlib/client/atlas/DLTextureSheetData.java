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

package de.mrjulsen.mcdragonlib.client.atlas;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.TextureFillMode;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class DLTextureSheetData {
    public static final AbstractSprite EMPTY_SPRITE = new StretchedSprite(new int[] {0, 0}, new int[] {0, 0});
    public static final DLTextureSheetData EMPTY = new DLTextureSheetData(Map.of("", EMPTY_SPRITE), new int[] {0, 0});
    public static final DLGuiTextureDataSerializer SERIALIZER = new DLGuiTextureDataSerializer();

    static {
        EMPTY.setTextureLocation(TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    public static abstract class AbstractSprite {
        private DLTextureSheetData data = EMPTY;
        void setMetadata(DLTextureSheetData data) {
            Objects.requireNonNull(data);
            this.data = data;
        }
        public DLTextureSheetData metadata() {
            return data;
        }

        @Deprecated(forRemoval = true)
        public void render(de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics graphics, int x, int y, int w, int h) {
            render(toModern(graphics), x, y, w, h);
        }

        @SuppressWarnings("removal")
        public void render(DLGuiGraphics graphics, int x, int y, int w, int h) {
            render(graphics.legacy(), x, y, w, h);
        }

        @Deprecated(forRemoval = true)
        private static DLGuiGraphics toModern(de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics legacy) {
            com.mojang.blaze3d.platform.Window window = net.minecraft.client.Minecraft.getInstance().getWindow();
            DLGuiGraphics graphics = new DLGuiGraphics();
            graphics.beginFrame(legacy.graphics(), legacy.defaultFont(), legacy.partialTick(), window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0, 0);
            return graphics;
        }
    }

    public static class StretchedSprite extends AbstractSprite {

        private final int[] uv;
        private final int[] size;

        public StretchedSprite(int[] uv, int[] size) {
            this.uv = uv;
            this.size = size;
        }

        public int u() {
            return uv[0];
        }

        public int v() {
            return uv[1];
        }

        public int width() {
            return size[0];
        }

        public int height() {
            return size[1];
        }

        @Override
        public void render(DLGuiGraphics graphics, int x, int y, int w, int h) {
            GuiUtils.texture(
                    graphics,
                    metadata().location(),
                    x, y, w, h,
                    u(),
                    v(),
                    width(),
                    height(),
                    metadata().width(),
                    metadata().height(),
                    TextureFillMode.STRETCH
            );
        }
    }

    public static class TiledSprite extends AbstractSprite {

        private final int[] uv;
        private final int[] size;

        public TiledSprite(int[] uv, int[] size) {
            this.uv = uv;
            this.size = size;
        }

        public int u() {
            return uv[0];
        }

        public int v() {
            return uv[1];
        }

        public int width() {
            return size[0];
        }

        public int height() {
            return size[1];
        }

        @Override
        public void render(DLGuiGraphics graphics, int x, int y, int w, int h) {
            GuiUtils.texture(
                graphics,
                metadata().location(),
                x, y, w, h,
                u(),
                v(),
                width(),
                height(),
                metadata().textureSize[0],
                metadata().textureSize[1],
                TextureFillMode.TILE
            );
        }
    }

    public static class NineSlicedSprite extends AbstractSprite {

        private final int[] uv;
        private final int[] size;
        private final int[] borderSize;
        private final boolean tiledContent;
        private final boolean tiledBorder;

        public NineSlicedSprite(int[] uv, int[] size, int[] borderSize, boolean tiledBorder, boolean tiledContent) {
            this.uv = uv;
            this.size = size;
            this.borderSize = borderSize;
            this.tiledContent = tiledContent;
            this.tiledBorder = tiledBorder;
        }

        public int u() {
            return uv[0];
        }

        public int v() {
            return uv[1];
        }

        public int width() {
            return size[0];
        }

        public int height() {
            return size[1];
        }

        public int leftBorder() {
            return borderSize[0];
        }

        public int topBorder() {
            return borderSize[1];
        }

        public int rightBorder() {
            return borderSize[2];
        }

        public int bottomBorder() {
            return borderSize[3];
        }

        public boolean tiledContent() {
            return tiledContent;
        }

        public boolean tiledBorder() {
            return tiledBorder;
        }

        @Override
        public void render(DLGuiGraphics graphics, int x, int y, int w, int h) {

            GuiUtils.texture(graphics, metadata().location(), x, y, leftBorder(), topBorder(), u(), v(), leftBorder(), topBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH); // Top Left
            GuiUtils.texture(graphics, metadata().location(), x + w - rightBorder(), y, rightBorder(), topBorder(), u() + width() - rightBorder(), v(), rightBorder(), topBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH); // Top Right
            GuiUtils.texture(graphics, metadata().location(), x, y + h - bottomBorder(), leftBorder(), bottomBorder(), u(), v() + height() - bottomBorder(), leftBorder(), bottomBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH); // Bottom Left
            GuiUtils.texture(graphics, metadata().location(), x + w - rightBorder(), y + h - bottomBorder(), rightBorder(), bottomBorder(), u() + width() - rightBorder(), v() + height() - bottomBorder(), rightBorder(), bottomBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH); // Bottom Right

            GuiUtils.texture(graphics, metadata().location(), x + leftBorder(), y, w - leftBorder() - rightBorder(), topBorder(), u() + leftBorder(), v(), width() - leftBorder() - rightBorder(), topBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH); // Top
            GuiUtils.texture(graphics, metadata().location(), x + leftBorder(), y + h - bottomBorder(), w - leftBorder() - rightBorder(), bottomBorder(), u() + leftBorder(), v() + height() - bottomBorder(), width() - leftBorder() - rightBorder(), bottomBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH); // Bottom
            GuiUtils.texture(graphics, metadata().location(), x, y + topBorder(), leftBorder(), h - topBorder() - bottomBorder(), u(), v() + topBorder(), leftBorder(), height() - topBorder() - bottomBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH); // Left
            GuiUtils.texture(graphics, metadata().location(), x + w - rightBorder(), y + topBorder(), rightBorder(), h - topBorder() - bottomBorder(), u() + width() - rightBorder(), v() + topBorder(), rightBorder(), height() - topBorder() - bottomBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH); // Right
            
            GuiUtils.texture(graphics, metadata().location(), x + rightBorder(), y + rightBorder(), w - rightBorder() - leftBorder(), h - topBorder() - bottomBorder(), u() + rightBorder(), v() + topBorder(), width() - rightBorder() - leftBorder(), height() - topBorder() - bottomBorder(), metadata().textureSize[0], metadata().textureSize[1], tiledContent() ? TextureFillMode.TILE : TextureFillMode.STRETCH);

        }
    }

    private final Map<String, AbstractSprite> sprites;
    private final int[] textureSize;
    private ResourceLocation location;

    public DLTextureSheetData(Map<String, AbstractSprite> sprites, int[] textureSize) {
        this.sprites = sprites;
        this.textureSize = textureSize;
        sprites.values().forEach(x -> x.setMetadata(this));
    }

    public Set<String> availableSprites() {
        return ImmutableSet.copyOf(sprites.keySet());
    }
    
    public AbstractSprite getSprite(String variant) {
        if (sprites.containsKey(variant)) {
            return sprites.get(variant);
        }
        if (sprites.containsKey("")) { // Fallback
            return sprites.get("");
        }
        return EMPTY_SPRITE;
    }

    public int width() {
        return textureSize[0];
    }

    public int height() {
        return textureSize[1];
    }

    public void setTextureLocation(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation location() {
        return location;
    }
}
