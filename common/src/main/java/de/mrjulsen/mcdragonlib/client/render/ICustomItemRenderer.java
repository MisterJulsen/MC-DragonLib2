package de.mrjulsen.mcdragonlib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.ber.RenderGraphics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

public interface ICustomItemRenderer {
    @Environment(EnvType.CLIENT)
    void renderAdditional(RenderGraphics graphics, ItemStack itemStack, ItemTransforms.TransformType transformType, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model);
}
