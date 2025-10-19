package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.BasicBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class DragonLibBlockEntityRenderer extends BasicBlockEntityRenderer<DragonLibBlockEntity>{


    public DragonLibBlockEntityRenderer(Context context) {
        super(context);
    }

    @Override
    protected void renderBlock(BERGraphics<DragonLibBlockEntity> graphics, float partialTicks) {
    }
    
}
