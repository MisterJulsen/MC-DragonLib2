package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class NetworkTest {

    public static class TestData extends NetworkPacketData {

        String txt = "";

        public TestData(DLStatus status) {
            super(status);
        }
        
        public TestData(DLStatus status, String txt) {
            super(status);
            DLNetworkManager.LOGGER.info("Send text message: " + txt);
            this.txt = txt;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putString("txt", txt);
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.txt = nbt.getString("txt");
        }

    }


    public static final DLNetworkManager NETWORK = new DLNetworkManager(DLUtils.resourceLocation(DragonLib.MODID, "network_test"), "1");

    public static final NetworkPacketType.Send<NetworkDirection.C2S, TestData> SEND = NETWORK.registerSendOnlyPacket("string_message", NetworkDirection.C2S,
        (data, ctx) -> {
            ctx.queue(() -> {
                DLNetworkManager.LOGGER.info("Text message is: " + data.txt);
            });
        }, TestData::new);

    public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, TestData, TestData> SEND_AND_RECEIVE = NETWORK.registerSendAndReceivePacket("test_msg", NetworkDirection.C2S,
            (data, ctx) -> {
                DLNetworkManager.LOGGER.info("Text message is: " + data.txt);

                try {
                    Thread.sleep(100000);
                } catch (InterruptedException e) {

                }
                int i = 1 / 0;
                return new TestData(data.getStatus(), data.txt);
            }, TestData::new, TestData::new);

    public static void init() {
    }

}
