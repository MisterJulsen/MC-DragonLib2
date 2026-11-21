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

package de.mrjulsen.mcdragonlib.client.ber;

import de.mrjulsen.mcdragonlib.client.util.FontUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockEntityRendererInstance<T extends BlockEntity> {

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    final FontUtils fontUtils = new FontUtils(Style.DEFAULT_FONT);

    /**
     * The rendering method.
     * @param graphics
     * @param partialTick
     */
    void render(BERGraphics<T> graphics, float partialTick);

    /**
     * Called every tick. Can be used for animations.
     * @param level
     * @param pos
     * @param state
     * @param blockEntity
     */
    default void tick(Level level, BlockPos pos, BlockState state, T blockEntity) { }

    /**
     * Called when the content of the BER changes. Can be used to perform recalculations only when necessary.
     * @param level
     * @param pos
     * @param state
     * @param blockEntity
     * @param data
     */
    default void update(Level level, BlockPos pos, BlockState state, T blockEntity, Object data) { }

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    default FontUtils getFontUtils() {
        return fontUtils;
    }
}
