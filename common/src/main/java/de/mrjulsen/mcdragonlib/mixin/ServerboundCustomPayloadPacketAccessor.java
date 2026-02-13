package de.mrjulsen.mcdragonlib.mixin;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class ServerboundCustomPayloadPacketAccessor {
    
    @Accessor("MAX_PAYLOAD_SIZE")
    public static int dragonlib$maxPayloadSize() {
        throw new AssertionError();
    }
}
