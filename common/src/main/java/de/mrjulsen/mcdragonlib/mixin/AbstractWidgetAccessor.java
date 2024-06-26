package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.AbstractWidget;

@Mixin(AbstractWidget.class)
public interface AbstractWidgetAccessor {
    
    @Accessor("height")
    void setHeight(int height);
}
