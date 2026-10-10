package de.mrjulsen.mcdragonlib.mixin;

import de.mrjulsen.mcdragonlib.DragonLib;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mrjulsen.mcdragonlib.block.IBlockEntityExtension;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Inject(method = "clearAllBlockEntities", at = @At(value = "HEAD"))
    public void dragonlib$onClearAllBlockEntities(CallbackInfo ci) {
        try {
            LevelChunk self = (LevelChunk)(Object)this;
            List<BlockEntity> bes = new ArrayList<>(self.getBlockEntities().values());
            for (BlockEntity be : bes) {
                if (be instanceof IBlockEntityExtension bext) {
                    bext.dragonlib$onChunkUnloaded();
                }
            }
        } catch (Throwable e) {
            DragonLib.LOGGER.error("Unable to clear block entities.", e);
        }
    }
    
    @Inject(method = "addAndRegisterBlockEntity", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/level/chunk/LevelChunk;updateBlockEntityTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    public void dragonlib$onAddAndRegisterBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        try {
            if (blockEntity instanceof IBlockEntityExtension be) {
                be.dragonlib$onBlockEntityLoad();
            }
        } catch (Throwable e) {
            DragonLib.LOGGER.error("Unable to add block entity.", e);
        }
    }
}
