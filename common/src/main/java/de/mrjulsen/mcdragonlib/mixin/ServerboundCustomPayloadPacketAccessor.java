package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class ServerboundCustomPayloadPacketAccessor {
    
    @Accessor("MAX_PAYLOAD_SIZE")
    public static int dragonlib$maxPayloadSize() {
        throw new AssertionError();
    }
}
