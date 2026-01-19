package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.mixin.client;

import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.IBakedQuadExtension;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MutableQuadViewImpl.class)
public abstract class MutableQuadViewImplMixin implements IBakedQuadExtension {

    private DLFaceData faceData;

    @Override
    public void dragonlib$setFaceData(DLFaceData data) {
        this.faceData = data;
    }

    @Override
    public DLFaceData dragonlib$getFaceData() {
        return faceData;
    }

    @Inject(method = "fromVanilla(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/core/Direction;)Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;", at = @At("RETURN"))
    public void dragonlib$onLoadFromVanilla(BakedQuad quad, RenderMaterial material, Direction cullFace, CallbackInfoReturnable<MutableQuadViewImpl> cir) {
        if (cir.getReturnValue() instanceof IBakedQuadExtension ext && quad instanceof IBakedQuadExtension ext2) {
            ext.dragonlib$setFaceData(ext2.dragonlib$getFaceData());
        }
    }
}
