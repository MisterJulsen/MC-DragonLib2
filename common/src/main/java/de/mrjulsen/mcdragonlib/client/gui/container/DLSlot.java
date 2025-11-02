package de.mrjulsen.mcdragonlib.client.gui.container;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents.DraggingOverEvent;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents.MouseDownEvent;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents.MouseReleaseEvent;
import de.mrjulsen.mcdragonlib.client.gui.properties.Property;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.manager.InventoryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;

public class DLSlot extends DLGuiComponent {

    public final Property<DLSprite> icon = new Property<DLSprite>(DLSprite.empty());

    public final AbstractContainerMenu menu;
    private final Slot slot;
    private final int slotIndex;

    public DLSlot(int x, int y, int w, int h, Slot sl, AbstractContainerMenu menu) {
        super(x, y, w, h);
        this.menu = menu;
        this.slot = sl;
        this.slotIndex = sl.index;

        addEventListener(DLGuiStandardEvents.MouseDownEvent.class, (s, e) -> {
            handleMouseDownEvent(e);
            return false;
        });

        addEventListener(DLGuiStandardEvents.MouseReleaseEvent.class, (sender, e) -> {
            handleMouseReleaseEvent(e);
            return false;
        });

        addEventListener(DLGuiStandardEvents.DraggingOverEvent.class, (sender, e) -> {
            handleMouseDraggedEvent(e);
            return false;
        });
    }

    protected void handleMouseDownEvent(MouseDownEvent e) {        
        InventoryManager manager = getWindowManager().addManager(InventoryManager::new);
        boolean pickItem = Minecraft.getInstance().options.keyPickItem.matchesMouse(e.button()) && Minecraft.getInstance().gameMode.hasInfiniteItems();
        Slot slot = menu.getSlot(slotIndex); 
        int slotIndex = slot.index;
        long clickTime = Util.getMillis();
        manager.setDoubleClick(manager.getLastClickedSlot() != null && manager.getLastClickedSlot().slot == slot && clickTime - manager.getLastClickTime() < 250L && manager.getLastClickButton() == e.button());
        manager.setSkipNextRelease(false);

        if ((Boolean) Minecraft.getInstance().options.touchscreen().get()) {
            if (slot != null && slot.hasItem()) {
                manager.setSelectedSlot(this);
                manager.setDraggingItem(ItemStack.EMPTY);
                manager.setIsSplittingStack(e.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            } else {
                manager.setSelectedSlot(null);
            }
        } else if (!manager.isQuickCrafting()) {
            if (manager.getHoldingItem().isEmpty()) {
                if (pickItem) {
                    this.slotClicked(slotIndex, e.button(), ClickType.CLONE);
                } else {
                    boolean qickMoving = slotIndex != AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT));
                    ClickType clickType = ClickType.PICKUP;
                    if (qickMoving) {
                        manager.setLastQuickMoved(slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY);
                        clickType = ClickType.QUICK_MOVE;
                    } else if (slotIndex == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE) {
                        clickType = ClickType.THROW;
                    }
                    this.slotClicked(slotIndex, e.button(), clickType);
                }

                manager.setSkipNextRelease(true);
            } else {
                manager.setQuickCrafting(true, e.button(), switch (e.button()) {
                    case GLFW.GLFW_MOUSE_BUTTON_LEFT -> AbstractContainerMenu.QUICKCRAFT_TYPE_CHARITABLE;
                    case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> AbstractContainerMenu.QUICKCRAFT_TYPE_GREEDY;
                    case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> AbstractContainerMenu.QUICKCRAFT_TYPE_CLONE;
                    default -> -1;
                });
            }
        }

        manager.setLastClickedSlot(this);
        manager.setLastClickTime(clickTime);
        manager.setLastClickedButton(e.button());
    }

    protected void handleMouseReleaseEvent(MouseReleaseEvent e) {
        InventoryManager manager = getWindowManager().addManager(InventoryManager::new);
        DLSlot targetDLSlot = null;
        for (DLGuiComponent c : getWindowManager().getDraggedOverComponents()) {
            if (c instanceof DLSlot s) {
                targetDLSlot = s;
                break;
            }
        }            
        
        if (targetDLSlot == null) {
            targetDLSlot = this; 
        }

        Slot targetSlot = targetDLSlot.slot;
        int targetSlotIndex = (targetSlot != null) ? targetSlot.index : -1;

        if (manager.isDoubleClick() && targetSlot != null && e.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, targetSlot)) {
            if (DLWindowManager.hasShiftDown()) {
                if (!manager.getQuickMovedItem().isEmpty()) {
                    for (Slot s : this.menu.slots) {
                        if (s != null && s.mayPickup(Minecraft.getInstance().player) && s.hasItem() && s.container == targetSlot.container && AbstractContainerMenu.canItemQuickReplace(s, manager.getQuickMovedItem(), true)) {
                            this.slotClicked(s.index, e.button(), ClickType.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked(targetSlotIndex, e.button(), ClickType.PICKUP_ALL);
            }
            manager.setSkipNextRelease(true);
        } else {
            if (manager.isQuickCrafting() && manager.getQuickCraftingButton() != e.button()) {
                manager.setQuickCrafting(false);
                manager.setSkipNextRelease(true);
                return;
            }
            if (manager.shouldSkipNextRelease()) {
                manager.setSkipNextRelease(false);
                return;
            }

            boolean canQuickReplace;
            if (manager.getSelectedSlot() != null && Minecraft.getInstance().options.touchscreen().get()) {
                if (e.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT || e.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    if (manager.getDraggingItem().isEmpty() && targetSlot != manager.getSelectedSlot().slot) {
                        manager.setDraggingItem(manager.getSelectedSlot().slot.getItem());
                    }
                    canQuickReplace = AbstractContainerMenu.canItemQuickReplace(targetSlot, manager.getDraggingItem(), false);
                    
                    if (targetSlotIndex != -1 && !manager.getDraggingItem().isEmpty() && canQuickReplace) {                            
                        this.slotClicked(manager.getSelectedSlot().slot.index, e.button(), ClickType.PICKUP);                            
                        this.slotClicked(targetSlotIndex, 0, ClickType.PICKUP);
                        
                        if (this.menu.getCarried().isEmpty()) {
                            manager.setSnapbackItem(ItemStack.EMPTY);
                        } else {                                
                            this.slotClicked(manager.getSelectedSlot().slot.index, e.button(), ClickType.PICKUP);
                            manager.setSnapbackStartX(Mth.floor(e.mouseX() + getXOnScreen())); 
                            manager.setSnapbackStartY(Mth.floor(e.mouseY() + getYOnScreen()));
                            manager.setSnapbackEnd(manager.getSelectedSlot());
                            manager.setSnapbackItem(manager.getDraggingItem());
                            manager.setSnapbackTime(Util.getMillis());
                        }
                    } else if (!manager.getDraggingItem().isEmpty()) {
                        
                        manager.setSnapbackStartX(Mth.floor(e.mouseX() + getXOnScreen())); 
                        manager.setSnapbackStartY(Mth.floor(e.mouseY() + getYOnScreen()));
                        manager.setSnapbackEnd(manager.getSelectedSlot());
                        manager.setSnapbackItem(manager.getDraggingItem());
                        manager.setSnapbackTime(Util.getMillis());
                    }
                    this.clearDraggingState(manager);
                }
            } else if (manager.isQuickCrafting() && !manager.getQuickCraftingSlots().isEmpty()) {
                this.slotClicked(AbstractContainerMenu.SLOT_CLICKED_OUTSIDE, AbstractContainerMenu.getQuickcraftMask(AbstractContainerMenu.QUICKCRAFT_HEADER_START, manager.getQuickCraftingType()), ClickType.QUICK_CRAFT);
                for (Slot s : manager.getQuickCraftingSlots()) {
                    this.slotClicked(s.index, AbstractContainerMenu.getQuickcraftMask(AbstractContainerMenu.QUICKCRAFT_HEADER_CONTINUE, manager.getQuickCraftingType()), ClickType.QUICK_CRAFT);
                }
                this.slotClicked(AbstractContainerMenu.SLOT_CLICKED_OUTSIDE, AbstractContainerMenu.getQuickcraftMask(AbstractContainerMenu.QUICKCRAFT_HEADER_END, manager.getQuickCraftingType()), ClickType.QUICK_CRAFT);
            } else if (!this.menu.getCarried().isEmpty()) {
                if (Minecraft.getInstance().options.keyPickItem.matchesMouse(e.button())) {
                    this.slotClicked(targetSlotIndex, e.button(), ClickType.CLONE); 
                } else {
                    canQuickReplace = targetSlotIndex != AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT));
                    if (canQuickReplace) {
                        manager.setLastQuickMoved(targetSlot != null && targetSlot.hasItem() ? targetSlot.getItem().copy() : ItemStack.EMPTY);
                    }

                    this.slotClicked(targetSlotIndex, e.button(), canQuickReplace ? ClickType.QUICK_MOVE : ClickType.PICKUP); 
                }
            }
        }

        if (this.menu.getCarried().isEmpty()) {
            manager.setLastClickTime(0);
        }

        manager.setQuickCrafting(false);
    }

    protected void handleMouseDraggedEvent(DraggingOverEvent e) {        
        DLSlot slot = null;
        if (e.other() != null) { 
            for (DLGuiComponent c : e.other()) {
                if (c instanceof DLSlot s) {
                    slot = s;
                    break;
                }
            }
        }

        InventoryManager manager = getWindowManager().addManager(InventoryManager::new);
        ItemStack itemStack = this.menu.getCarried();
        if (manager.getSelectedSlot() != null && (Boolean) Minecraft.getInstance().options.touchscreen().get()) {
            if (e.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT || e.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (manager.getDraggingItem().isEmpty()) {
                    boolean stillOverOriginalSlot = (slot != null && slot == manager.getSelectedSlot());
                    
                    if (!stillOverOriginalSlot && !manager.getSelectedSlot().slot.getItem().isEmpty()) {
                        manager.setDraggingItem(manager.getSelectedSlot().slot.getItem().copy());
                    }
                } else if (manager.getDraggingItem().getCount() > 1 && slot != null && AbstractContainerMenu.canItemQuickReplace(slot.slot, manager.getDraggingItem(), false)) {
                    long l = Util.getMillis();
                    if (manager.getQuickdropSlot() == slot.slot) {
                        if (l - manager.getQuickdropTime() > 500L) {
                            this.slotClicked(manager.getSelectedSlot().slot.index, 0, ClickType.PICKUP);
                            this.slotClicked(slot.slotIndex, 1, ClickType.PICKUP);
                            this.slotClicked(manager.getSelectedSlot().slot.index, 0, ClickType.PICKUP);
                            manager.setQuickdropTime(l + 750L);
                            manager.getDraggingItem().shrink(1);
                        }
                    } else {
                        manager.setQuickdropSlot(slot.slot);
                        manager.setQuickdropTime(l);
                    }
                }
            }
        } else if (manager.isQuickCrafting() && slot != null && !itemStack.isEmpty()
                && (itemStack.getCount() > manager.getQuickCraftingSlots().size() || manager.getQuickCraftingType() == 2)
                && AbstractContainerMenu.canItemQuickReplace(slot.slot, itemStack, true) && slot.slot.mayPlace(itemStack)
                && this.menu.canDragTo(slot.slot)) {
            manager.getQuickCraftingSlots().add(this.slot);
            manager.getQuickCraftingSlots().add(slot.slot);
            this.recalculateQuickCraftRemaining(manager);
        }
    }

    private void recalculateQuickCraftRemaining(InventoryManager manager) {
        ItemStack itemStack = this.menu.getCarried();
        if (!itemStack.isEmpty() && manager.isQuickCrafting()) {
            if (manager.getQuickCraftingType() == 2) {
                manager.setQuickCraftingRemainder(itemStack.getMaxStackSize());
            } else {
                int remainder = itemStack.getCount();
                for (Slot slot : manager.getQuickCraftingSlots()) {
                    ItemStack slotStack = slot.getItem();
                    int count = slotStack.isEmpty() ? 0 : slotStack.getCount();
                    int maxCount = Math.min(itemStack.getMaxStackSize(), slot.getMaxStackSize(itemStack));
                    int usedCount = Math.min(AbstractContainerMenu.getQuickCraftPlaceCount(
                            manager.getQuickCraftingSlots(), manager.getQuickCraftingType(), itemStack) + count, maxCount);
                    remainder -= usedCount - count;
                }
                manager.setQuickCraftingRemainder(remainder);
            }
        }
    }

    protected void slotClicked(int slotId, int mouseButton, ClickType type) {
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(menu.containerId, slotId, mouseButton, type, Minecraft.getInstance().player);
    }

    public void clearDraggingState(InventoryManager manager) {
        manager.setDraggingItem(ItemStack.EMPTY);
        manager.setSelectedSlot(null);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        InventoryManager manager = getWindowManager().addManager(InventoryManager::new);
        ItemStack itemStack = slot.getItem();
        ItemStack carriedStack = manager.getHoldingItem();
        String string = null;
        boolean renderHighlight = false;
        boolean renderEmptyIcon = false;

        if (manager.getSelectedSlot() != null && slot == manager.getSelectedSlot().slot && !manager.getDraggingItem().isEmpty() && manager.isSplittingStack() && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        }
        else if (manager.isQuickCrafting() && manager.getQuickCraftingSlots().contains(this.slot) && !carriedStack.isEmpty()) {
            if (manager.getQuickCraftingSlots().size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(slot, carriedStack, true) && this.menu.canDragTo(slot)) {
                renderHighlight = true;
                int maxStackSize = Math.min(carriedStack.getMaxStackSize(), slot.getMaxStackSize(carriedStack));
                int stackSize = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                int count = AbstractContainerMenu.getQuickCraftPlaceCount(manager.getQuickCraftingSlots(), manager.getQuickCraftingType(), carriedStack) + stackSize;

                if (count > maxStackSize) {
                    count = maxStackSize;
                    String maxStackSizeTxt = ChatFormatting.YELLOW.toString();
                    string = maxStackSizeTxt + maxStackSize;
                }

                itemStack = carriedStack.copyWithCount(count);
            } else {
                manager.getQuickCraftingSlots().remove(this.slot);
                this.recalculateQuickCraftRemaining(manager);
            }
        }

        DefaultGuiTextures.DRAGONLIB_UI.getSprite("slot").render(graphics, 0, 0, width(), height());
        if (itemStack.isEmpty() && slot.isActive()) {
            DLSprite sprite = icon.get();
            sprite.render(graphics, width() / 2 - sprite.getWidth() / 2, height() / 2 - sprite.getHeight() / 2);
        }

        if (!renderEmptyIcon) {
            if (renderHighlight) {
                GuiUtils.fill(graphics, 1, 1, 16, 16, DLColor.fromInt(-2130706433));
            }
            GuiUtils.renderItem(graphics, itemStack, 1, 1, 1, true);
            GuiUtils.renderItemDecoration(graphics, itemStack, 1, 1, 1, string);
        }

        if (isSelected()) {
            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0, 0, 32700);
            GuiUtils.fill(graphics, 1, 1, 16, 16, DLColor.fromInt(-2130706433));
            graphics.poseStack().popPose();
        }
    }

    @Override
    public void renderFrontLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (isSelected()) {
            InventoryManager manager = getWindowManager().addManager(InventoryManager::new);
            if (manager == null || (!manager.isHoldingItem() && manager.getDraggingItem().isEmpty())) {
                ItemStack itemstack = menu.getSlot(slotIndex).getItem();
                if (!itemstack.isEmpty()) {
                    Minecraft mc = Minecraft.getInstance();
                    graphics.graphics().renderTooltip(mc.font, getTooltipFromItem(itemstack), itemstack.getTooltipImage(), (int) mouseX, (int) mouseY);
                }
            }
        }
    }

    public static List<Component> getTooltipFromItem(ItemStack item) {
        return item.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL);
    }
}