package de.mrjulsen.mcdragonlib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mrjulsen.mcdragonlib.client.util.DLGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface ICustomItemRenderer {
    void renderAdditional(DLGraphics graphics, ItemStack itemStack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model);
}
