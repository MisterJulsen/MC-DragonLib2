package de.mrjulsen.mcdragonlib.menu;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.internal.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class PlayerInventoryContainerMenu extends AbstractContainerMenu {

    public final ContainerLevelAccess access;

    public PlayerInventoryContainerMenu(MenuType<?> type, int containerId, Inventory inv) {
        this(type, containerId, inv, ContainerLevelAccess.NULL);
    }

    public PlayerInventoryContainerMenu(MenuType<?> type, int containerId, Inventory inv, final ContainerLevelAccess access) {
        super(type, containerId);
        this.access = access;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }
    
    protected void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 0, 0));
            }
        }
    }

    protected void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 0, 0));
        }
    }



    public static class Base extends PlayerInventoryContainerMenu {
        public Base(int containerId, Inventory inv) {
            this(containerId, inv, ContainerLevelAccess.NULL);
        }        

        public Base(int containerId, Inventory inv, final ContainerLevelAccess access) {
            super(ModMenuTypes.PLAYER_INVENTORY.get(), containerId, inv, access);
        }        

        @Override
        public boolean stillValid(Player player) {
            return stillValid(this.access, player, DragonLib.DRAGON_BLOCK.get());
        }
        
        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            ItemStack itemstack = ItemStack.EMPTY;
            Slot slot = this.slots.get(index);

            if (slot != null && slot.hasItem()) {
                ItemStack stackInSlot = slot.getItem();
                itemstack = stackInSlot.copy();

                if (index < 9) {
                    if (!this.moveItemStackTo(stackInSlot, 9, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                
                else if (index < 36) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                        return ItemStack.EMPTY;
                    }
                }

                if (stackInSlot.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                if (stackInSlot.getCount() == itemstack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.onTake(player, stackInSlot);
            }

            return itemstack;
        }
        
    }
}
