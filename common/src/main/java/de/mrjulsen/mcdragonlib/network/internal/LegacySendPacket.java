package de.mrjulsen.mcdragonlib.network.internal;

import java.util.Objects;
import java.util.function.Function;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import de.mrjulsen.mcdragonlib.net.PacketDefinition;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Carries a deprecated send-only packet over the current transport.
 *
 * @param <I> the payload type
 */
@Deprecated
public final class LegacySendPacket<I extends NetworkPacketData> extends PacketDefinition {

    private final DLStreamCodec<I> codec;
    private final NetworkProcessor.Send<I> processor;

    public LegacySendPacket(Settings settings, Function<DLStatus, I> factory, NetworkProcessor.Send<I> processor) {
        super(settings);
        this.codec = LegacyBridge.dataCodec(factory);
        this.processor = Objects.requireNonNull(processor, "processor");
    }

    /**
     * Sends a payload through a legacy sender.
     *
     * @param sender where the payload goes
     * @param data the value to send
     */
    public void send(NetworkDirection sender, I data) {
        LegacyBridge.send(getChannel(), this, sender, buf -> codec.encode(buf, data));
    }

    @Override
    protected void receive(PacketContext context, FriendlyByteBuf payload) {
        I data;
        try {
            data = codec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode {}.", this, e);
            return;
        }
        getHandlerExecutionMode().run(context, () -> {
            try {
                processor.execute(data, LegacyBridge.adapt(context));
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Handler of {} failed.", this, e);
            }
        });
    }
}
