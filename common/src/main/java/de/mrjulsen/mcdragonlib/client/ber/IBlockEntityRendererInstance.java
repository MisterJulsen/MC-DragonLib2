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
