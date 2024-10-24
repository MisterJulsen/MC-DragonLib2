package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.ber.BERCube;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.BasicBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class DragonLibBlockEntityRenderer extends BasicBlockEntityRenderer<DragonLibBlockEntity>{

    BERCube cube;

    public DragonLibBlockEntityRenderer(Context context) {
        super(context);
        cube = BERCube.fullCube(new ResourceLocation("textures/block/red_glazed_terracotta.png"), 1, 0.75f, 0.5f);
        cube.setAmbienOcclusion(true);
    }

    @Override
    protected void renderBlock(BERGraphics<DragonLibBlockEntity> graphics, float partialTicks) {
        graphics.poseStack().scale(16, 16, 16);
        //BERUtils.renderTexture(new ResourceLocation("textures/block/red_glazed_terracotta.png"), graphics.blockEntity(), graphics, true, 0, 0, 0, 1, 1, 0, 0, 1, 1, Direction.NORTH, 0xFFFFFFFF);
        cube.render(graphics);
    }
    
}
