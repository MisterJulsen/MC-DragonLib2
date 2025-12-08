package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.BERLabel;
import de.mrjulsen.mcdragonlib.client.ber.BasicBlockEntityRenderer;
import de.mrjulsen.mcdragonlib.client.ber.BERLabel.EScrollMode;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class DragonLibBlockEntityRenderer extends BasicBlockEntityRenderer<DragonLibBlockEntity> {
    private final BERLabel label = new BERLabel();

    public DragonLibBlockEntityRenderer(Context context) {
        super(context);
        label.text.set(TextUtils.text("DragonLib ").withStyle(ChatFormatting.GOLD).append(TextUtils.text("🐉").withStyle(ChatFormatting.RED)));
        label.y.set(11f);
        label.fullBackground.set(false);
        label.horizontalScrollingSpeed.set(8f);
        label.horizontalAlign.set(ETextAlignment.LEFT);
        label.horizontalMaxScale.set(0.5f);
        label.horizontalMinScale.set(0.5f);
        label.verticalMaxScale.set(0.5f);
        label.verticalMinScale.set(0.5f);
        label.horizontalScrollMode.set(EScrollMode.ALWAYS);
    }


    @Override
    protected void renderBlock(BERGraphics<DragonLibBlockEntity> graphics, float partialTick) {
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, 16.01f);
        label.render(graphics);
        graphics.poseStack().popPose();
    }
}
