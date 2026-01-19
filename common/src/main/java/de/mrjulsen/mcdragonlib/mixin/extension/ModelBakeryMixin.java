package de.mrjulsen.mcdragonlib.mixin.extension;

import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedModelWrapper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl")
public class ModelBakeryMixin {

    @Inject(method = "bake", at = @At("RETURN"), cancellable = true)
    public void dragonlib$onBake(ResourceLocation location, ModelState transform, CallbackInfoReturnable<BakedModel> cir) {
        cir.setReturnValue(DLBakedModelWrapper.wrap(cir.getReturnValue()));
    }
}
