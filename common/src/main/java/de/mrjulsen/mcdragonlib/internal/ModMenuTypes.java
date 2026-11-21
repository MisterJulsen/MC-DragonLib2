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

package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.menu.PlayerInventoryContainerMenu;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(DragonLib.MODID, Registries.MENU);
    public static final RegistrySupplier<MenuType<PlayerInventoryContainerMenu.Base>> PLAYER_INVENTORY = registerMenuType(PlayerInventoryContainerMenu.Base::new, "player_inventory");

    private static <T extends AbstractContainerMenu>RegistrySupplier<MenuType<T>> registerMenuType(MenuSupplier<T> factory, String name) {
        return MENUS.register(name, () -> new MenuType<T>(factory, FeatureFlags.DEFAULT_FLAGS));
    }

    public static void register() {
        MENUS.register();
    }

}