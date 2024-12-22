package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.ber.RenderGraphics;
import de.mrjulsen.mcdragonlib.client.render.ICustomItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1, shift = Shift.BEFORE))
    public void onRender(ItemStack itemStack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
		if (itemStack.getItem() instanceof ICustomItemRenderer renderer) {
            poseStack.pushPose();
            RenderGraphics graphics = new RenderGraphics(poseStack, buffer, combinedLight, combinedOverlay);
            poseStack.scale(DragonLib.PIXEL, DragonLib.PIXEL, DragonLib.PIXEL);
            poseStack.pushPose();
            renderer.renderAdditional(graphics, itemStack, context, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
            poseStack.popPose();
            poseStack.popPose();
        }
	}
}