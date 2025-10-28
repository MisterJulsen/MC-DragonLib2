package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.mixin.client;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockModel.class)
public interface BlockModelAccessor {

	@Accessor("FACE_BAKERY")
	public static FaceBakery dragonlib$getFaceBakery() {
		throw new AssertionError();
	}

	@Accessor("parent")
	BlockModel dragonlib$getParent();
}
