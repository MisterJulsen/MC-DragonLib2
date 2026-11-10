package de.mrjulsen.mcdragonlib.mixin.client;

import net.minecraft.client.StringSplitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StringSplitter.class)
public interface StringSplitterAccessor {

    @Accessor("widthProvider")
    StringSplitter.WidthProvider dragonlib$getWidthProvider();
}
