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