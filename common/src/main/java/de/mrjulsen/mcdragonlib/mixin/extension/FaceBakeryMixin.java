package de.mrjulsen.mcdragonlib.mixin.extension;

import de.mrjulsen.mcdragonlib.client.model.extension.IBakedQuadExtension;
import de.mrjulsen.mcdragonlib.client.model.extension.IBlockFaceElementExtension;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FaceBakery.class)
public class FaceBakeryMixin {

    @Inject(method = "bakeQuad", at = @At("RETURN"))
    public void dragonlib$onBakeQuad(Vector3f posFrom, Vector3f posTo, BlockElementFace face, TextureAtlasSprite sprite, Direction facing, ModelState transform, BlockElementRotation partRotation, boolean shade, ResourceLocation modelLocation, CallbackInfoReturnable<BakedQuad> cir) {
        if (cir.getReturnValue() instanceof IBakedQuadExtension ext && face instanceof IBlockFaceElementExtension ext2) {
            ext.dragonlib$setFaceData(ext2.dragonlib$getExtensionData());
        }
    }
}
