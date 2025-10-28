package de.mrjulsen.mcdragonlib.network;

import dev.architectury.utils.Env;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface NetworkPacketContext {
    Player getPlayer();    
    void queue(Runnable runnable);    
    Env getEnvironment();

    default NetworkDirection buildDirection() {
        return getEnvironment() == Env.CLIENT ? NetworkDirection.toServer() : NetworkDirection.toPlayer((ServerPlayer)getPlayer());
    }
}