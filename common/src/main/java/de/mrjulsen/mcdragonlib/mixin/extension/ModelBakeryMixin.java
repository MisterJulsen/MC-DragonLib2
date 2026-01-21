package de.mrjulsen.mcdragonlib.mixin.extension;

import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedModelWrapper;
import dev.architectury.injectables.annotations.PlatformOnly;
import dev.architectury.platform.Platform;
import net.fabricmc.fabric.impl.client.model.ModelLoaderHooks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Map;
import java.util.function.Function;

@Mixin(targets = "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl")
public abstract class ModelBakeryMixin {


    @Inject(method = "bake", at = @At("RETURN"), cancellable = true)
    public void dragonlib$onBake(ResourceLocation location, ModelState transform, CallbackInfoReturnable<BakedModel> cir) {
        //cir.setReturnValue(DLBakedModelWrapper.wrap(cir.getReturnValue()));
    }

    /*
    @PlatformOnly(PlatformOnly.FABRIC)
    @ModifyVariable(
            method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/UnbakedModel;bake(Lnet/minecraft/client/resources/model/ModelBaker;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;",
                    ordinal = 1
            ),
            ordinal = 0)
    private BakedModel dragonlib$fabric$wrapBakedModel(BakedModel original) {
        return DLBakedModelWrapper.wrap(original);
    }

    @PlatformOnly(PlatformOnly.FORGE)
    @ModifyVariable(
            method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;Ljava/util/function/Function;)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/UnbakedModel;bake(Lnet/minecraft/client/resources/model/ModelBaker;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;"
            ),
            ordinal = 0
    )
    private BakedModel dragonlib$forge$wrapBakedModel(BakedModel original) {
        System.out.println("HALLO");
        return DLBakedModelWrapper.wrap(original);
    }

     */


    /*
    @Redirect(
            method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/UnbakedModel;bake(Lnet/minecraft/client/resources/model/ModelBaker;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;"
            )
    )
    private BakedModel wrapBakingResult(UnbakedModel instance, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> materialTextureAtlasSpriteFunction, ModelState modelState, ResourceLocation resourceLocation) {
        BakedModel originalBakedModel = instance.bake(modelBaker, materialTextureAtlasSpriteFunction, modelState, resourceLocation);
        if (originalBakedModel != null) {
            return DLBakedModelWrapper.wrap(originalBakedModel);
        }
        return originalBakedModel;
    }

     */
}
