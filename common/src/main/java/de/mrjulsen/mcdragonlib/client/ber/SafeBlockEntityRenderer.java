package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class SafeBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

	private final BlockEntityRendererProvider.Context context;

	public SafeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public final void render(T be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
		if (isInvalid(be)) {
			return;
        }
		renderSafe(new BERGraphics<>(be, ms, bufferSource, context, light, overlay), partialTicks);
	}

	protected abstract void renderSafe(BERGraphics<T> graphics, float partialTicks);

	public boolean isInvalid(T be) {
		return !be.hasLevel() || be.getBlockState().getBlock() == Blocks.AIR;
	}
}
