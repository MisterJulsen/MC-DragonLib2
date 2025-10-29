package de.mrjulsen.mcdragonlib.mixin;

import de.mrjulsen.mcdragonlib.item.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.item.Item;

@Mixin(Item.class)
public class ItemMixin implements IItemExtension {
}

