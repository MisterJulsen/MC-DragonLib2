/*
 * This file is part of architectury.
 * Copyright (C) 2020, 2021, 2022 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.mrjulsen.mcdragonlib.network.fabric;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;

public class DLNetworkManagerImpl {
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(DragonLib.MODID, "network");
        
    public static void init() {        
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL_ID, (server, player, handler, buf, sender) -> {
            NetworkPacketContext context = context(player, server, false);
            DLNetworkManager.receiveData(buf, Side.C2S, context);             
        });
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_ID, new ClientPlayNetworking.PlayChannelHandler() {
            @Override
            public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
                NetworkPacketContext context = context(client.player, client, true);
                DLNetworkManager.receiveData(buf, Side.S2C, context);
            }
        });
    }
    
    
    private static NetworkPacketContext context(Player player, BlockableEventLoop<?> taskQueue, boolean client) {
        return new NetworkPacketContext() {
            @Override
            public Player getPlayer() {
                return player;
            }
            
            @Override
            public void queue(Runnable runnable) {
                taskQueue.execute(runnable);
            }
            
            @Override
            public Env getEnvironment() {
                return client ? Env.CLIENT : Env.SERVER;
            }
        };
    }
    
    public static Packet<?> toPacket(NetworkManager.Side side, FriendlyByteBuf buf) {
        if (side == NetworkManager.Side.C2S) {
            return toC2SPacket(buf);
        } else if (side == NetworkManager.Side.S2C) {
            return toS2CPacket(buf);
        }
        
        throw new IllegalArgumentException("Invalid side: " + side);
    }
    

    
    @Environment(EnvType.CLIENT)
    private static Packet<?> toC2SPacket(FriendlyByteBuf buf) {
        return ClientPlayNetworking.createC2SPacket(CHANNEL_ID, buf);
    }
    
    private static Packet<?> toS2CPacket(FriendlyByteBuf buf) {
        return ServerPlayNetworking.createS2CPacket(CHANNEL_ID, buf);
    }
}
