package de.mrjulsen.mcdragonlib.client.ber;

import de.mrjulsen.mcdragonlib.block.IBERInstance;
import de.mrjulsen.mcdragonlib.client.util.BERUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StaticBlockEntityRenderer<T extends BlockEntity & IBERInstance<T>> extends RotatableBlockEntityRenderer<T> {


    public StaticBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderBlock(BERGraphics<T> graphics, float partialTick) {
        BERUtils.initRenderEngine();
        graphics.blockEntity().getRenderer().render(graphics, partialTick);
    }

}
