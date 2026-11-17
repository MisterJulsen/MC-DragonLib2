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

package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.math.Axis;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class RotatableBlockEntityRenderer<T extends BlockEntity> extends SafeBlockEntityRenderer<T> {
    
    protected final Font font;

    public RotatableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
    }

    @Override
    protected final void renderSafe(BERGraphics<T> graphics, float partialTick) {
        BlockState blockState = graphics.blockEntity().getBlockState();
        
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0.5D, 0, 0.5F);
        graphics.poseStack().mulPose(Axis.YP.rotationDegrees(
            blockState.getValue(HorizontalDirectionalBlock.FACING) == Direction.EAST || blockState.getValue(HorizontalDirectionalBlock.FACING) == Direction.WEST
                ? blockState.getValue(HorizontalDirectionalBlock.FACING).getOpposite().toYRot()
                : blockState.getValue(HorizontalDirectionalBlock.FACING).toYRot()
        ));
        graphics.poseStack().translate(-0.5f, 1, -0.5f);
        graphics.poseStack().scale(0.0625f, -0.0625f, 0.0625f);

        renderBlock(graphics, partialTick);

        graphics.poseStack().popPose();
        
    }

    protected abstract void renderBlock(BERGraphics<T> graphics, float partialTick);
}
