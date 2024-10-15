package de.mrjulsen.mcdragonlib.client.ber;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class BasicBlockEntityRenderer<T extends BlockEntity> extends SafeBlockEntityRenderer<T> {
    
    protected final Font font;

    public BasicBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
    }

    @Override
    protected final void renderSafe(BERGraphics<T> graphics, float partialTick) {        
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0.5D, 0, 0.5F);
        graphics.poseStack().translate(-0.5f, 1, -0.5f);
        graphics.poseStack().scale(0.0625f, -0.0625f, 0.0625f);

        renderBlock(graphics, partialTick);

        graphics.poseStack().popPose();
        
    }

    protected abstract void renderBlock(BERGraphics<T> graphics, float partialTick);
}
