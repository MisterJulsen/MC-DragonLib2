package de.mrjulsen.mcdragonlib.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

/** Fixes fabric */
@Mixin(TextureManager.class)
public class TextureManagerMixin {

    @Final
    @Shadow
    private Map<ResourceLocation, AbstractTexture> byPath;

    @PlatformOnly(value = PlatformOnly.FABRIC)
    @Inject(method = "release", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;releaseTextureId(I)V", shift = Shift.BEFORE))
    public void onRelease(ResourceLocation path, CallbackInfo ci) {
        this.byPath.remove(path);
    }
}
