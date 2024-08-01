package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.math.Vector3f;

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
        graphics.poseStack().mulPose(Vector3f.YP.rotationDegrees(
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
