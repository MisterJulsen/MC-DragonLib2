/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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