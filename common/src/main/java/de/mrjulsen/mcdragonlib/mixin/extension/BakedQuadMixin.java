package de.mrjulsen.mcdragonlib.mixin.extension;

import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.IBakedQuadExtension;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements IBakedQuadExtension {

    private DLFaceData faceData;

    @Override
    public void dragonlib$setFaceData(DLFaceData data) {
        this.faceData = data;
    }

    @Override
    public DLFaceData dragonlib$getFaceData() {
        return faceData;
    }
}
