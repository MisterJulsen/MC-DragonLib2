package de.mrjulsen.mcdragonlib.mixin.extension;

import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.IBlockFaceElementExtension;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockElementFace.class)
public class BlockFaceElementMixin implements IBlockFaceElementExtension {

    private DLFaceData faceData;

    @Override
    public void dragonlib$setExtensionData(DLFaceData data) {
        this.faceData = data;
    }

    @Override
    public DLFaceData dragonlib$getExtensionData() {
        return faceData;
    }
}
