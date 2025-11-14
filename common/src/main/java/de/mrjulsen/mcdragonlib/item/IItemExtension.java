package de.mrjulsen.mcdragonlib.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IItemExtension {
    /**
     * Invoked every tick when this item is equipped.
     * @param stack the item stack
     * @param player the player wearing the armor
     */
    default void dragonlib$tickArmor(ItemStack stack, Player player) {
    }
    
    /**
     * Returns the {@link EquipmentSlot} for {@link ItemStack}.
     * @param stack the item stack
     * @return the {@link EquipmentSlot}, return {@code null} to default to vanilla's {@link net.minecraft.world.entity.Mob#getEquipmentSlotForItem(ItemStack)}
     */
    @Nullable
    default EquipmentSlot dragonlib$getCustomEquipmentSlot(ItemStack stack) {
        return null;
    }
}