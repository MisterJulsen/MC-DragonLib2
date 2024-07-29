package de.mrjulsen.mcdragonlib.client.ber;

import de.mrjulsen.mcdragonlib.block.IBERInstance;
import de.mrjulsen.mcdragonlib.client.ber.IBlockEntityRendererInstance.BlockEntityRendererContext;
import de.mrjulsen.mcdragonlib.client.util.BERUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StaticBlockEntityRenderer<T extends BlockEntity & IBERInstance<T>> extends RotatableBlockEntityRenderer<T> {

    private final BlockEntityRendererContext context;

    public StaticBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.context = new BlockEntityRendererContext(context, new BERUtils());
    }

    @Override
    protected void renderBlock(T pBlockEntity, float pPartialTicks, BERGraphics graphics) {
        context.renderUtils().initRenderEngine();
        pBlockEntity.getRenderer().render(context, pBlockEntity, pPartialTicks, graphics);
    }

}
