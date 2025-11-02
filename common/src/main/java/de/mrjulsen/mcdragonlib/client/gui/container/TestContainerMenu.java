package de.mrjulsen.mcdragonlib.client.gui.container;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TestContainerMenu extends AbstractContainerMenu {

    public final ContainerLevelAccess access;

    public TestContainerMenu(int pContainerId, Inventory inv) {
        this(pContainerId, inv, ContainerLevelAccess.NULL);
    }

    public TestContainerMenu(int pContainerId, Inventory inv, final ContainerLevelAccess access) {
        super(ModMenuTypes.TEST_MENU.get(), pContainerId);
        this.access = access;

        addPlayerHotbar(inv);
        addPlayerInventory(inv);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, DragonLib.DRAGON_BLOCK.get());
    }

    public void removed(Player pPlayer) {
        super.removed(pPlayer);
    }
    
    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 0, 0));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 0, 0));
        }
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 0;  // must be the number of slots you have!

    @Override
public ItemStack quickMoveStack(Player player, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    if (slot != null && slot.hasItem()) {
        ItemStack stackInSlot = slot.getItem();
        itemstack = stackInSlot.copy();

        // Hotbar (0–8) → Main Inventory (9–35)
        if (index < 9) {
            if (!this.moveItemStackTo(stackInSlot, 9, 36, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Main Inventory (9–35) → Hotbar (0–8)
        else if (index < 36) {
            if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
        }

        // Slot leeren oder aktualisieren
        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        // Wenn sich nichts verändert hat, abbrechen
        if (stackInSlot.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);
    }

    return itemstack;
}

}
