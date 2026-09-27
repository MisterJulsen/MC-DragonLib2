package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;

@Mixin(Connection.class)
public interface ConnectionAccessor {

    @Accessor("channel")
    Channel dragonlib$getChannel();
}
