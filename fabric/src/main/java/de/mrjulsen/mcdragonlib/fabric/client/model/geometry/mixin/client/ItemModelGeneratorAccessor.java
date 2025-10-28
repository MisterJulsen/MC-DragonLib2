package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.SpriteContents;

@Mixin(ItemModelGenerator.class)
public interface ItemModelGeneratorAccessor {

    @Invoker("processFrames")
    List<BlockElement> dragonlib$processFrames(int tintIndex, String texture, SpriteContents sprite);
    
}
