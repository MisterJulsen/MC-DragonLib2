package de.mrjulsen.mcdragonlib.mixin;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadPacketAccessor {   

    @Accessor("MAX_PAYLOAD_SIZE")
    public static int dragonlib$maxPayloadSize() {
        throw new AssertionError();
    }
}
