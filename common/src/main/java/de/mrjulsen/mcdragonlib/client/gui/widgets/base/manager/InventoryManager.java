package de.mrjulsen.mcdragonlib.client.gui.widgets.base.manager;

import java.util.HashSet;
import java.util.Set;

import de.mrjulsen.mcdragonlib.client.gui.container.DLSlot;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.IGuiManagementComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InventoryManager implements IGuiManagementComponent {

    public final DLWindowManager windowManager;

    protected final Set<Slot> quickCraftSlots = new HashSet<>();
    private boolean isQuickCrafting = false;
    private int quickCraftingButton = -1;
    private int quickCraftingType = -1;
    private int quickCraftingRemainder = 0;

    private DLSlot lastClickedSlot;
    private long lastClickedTime;
    private int lastClickedButton;
    private boolean doubleClick;

    private ItemStack quickMovedItem = ItemStack.EMPTY;
    private boolean skipNextRelease;

    private ItemStack draggingStack = ItemStack.EMPTY;
    private boolean isSplittingStack;
    private DLSlot clickedSlot;

    private DLSlot snapbackEnd;
    private int snapbackStartX;
    private int snapbackStartY;
    private long snapbackTime;
    private ItemStack snapbackItem = ItemStack.EMPTY;
    private Slot quickdropSlot;
    private long quickdropTime;


    public InventoryManager(DLWindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    public boolean isHoldingItem() {
        return lastClickedSlot == null ? false : !lastClickedSlot.menu.getCarried().isEmpty();
    }

    public ItemStack getHoldingItem() {
        ItemStack ret = lastClickedSlot != null ? lastClickedSlot.menu.getCarried() : ItemStack.EMPTY;
        return ret;
    }

    public boolean isQuickCrafting() {
        return isQuickCrafting;
    }

    public void setQuickCrafting(boolean b, int button, int type) {
        setQuickCrafting(b);
        this.quickCraftingButton = button;
        this.quickCraftingType = type;
    }

    public void setQuickCrafting(boolean b) {
        this.isQuickCrafting = b;
        this.quickCraftSlots.clear();
    }

    public int getQuickCraftingButton() {
        return quickCraftingButton;
    }

    public int getQuickCraftingType() {
        return quickCraftingType;
    }

    public Set<Slot> getQuickCraftingSlots() {
        return quickCraftSlots;
    }

    public ItemStack getQuickMovedItem() {
        return quickMovedItem;
    }

    public void setLastQuickMoved(ItemStack quickMovedItem) {
        this.quickMovedItem = quickMovedItem;
    }

    public void setQuickCraftingRemainder(int value) {
        this.quickCraftingRemainder = value;
    }

    public int getQuickCraftingRemainder() {
        return quickCraftingRemainder;
    }

    public void setSkipNextRelease(boolean b) {
        this.skipNextRelease = b;
    }

    public boolean shouldSkipNextRelease() {
        return skipNextRelease;
    }

    public void setLastClickedSlot(DLSlot slot) {
        this.lastClickedSlot = slot;
    }

    public void setLastClickTime(long time) {
        this.lastClickedTime = time;
    }

    public void setLastClickedButton(int button) {
        this.lastClickedButton = button;
    }

    public long getLastClickTime() {
        return lastClickedTime;
    }

    public int getLastClickButton() {
        return lastClickedButton;
    }

    public void setDoubleClick(boolean b) {
        this.doubleClick = b;
    }

    public boolean isDoubleClick() {
        return doubleClick;
    }

    public DLSlot getLastClickedSlot() {
        return lastClickedSlot;
    }

    public void setDraggingItem(ItemStack stack) {
        this.draggingStack = stack;
    }

    public ItemStack getDraggingItem() {
        return draggingStack;
    }

    public void setIsSplittingStack(boolean b) {
        this.isSplittingStack = b;
    }

    public boolean isSplittingStack() {
        return isSplittingStack;
    }

    public void setSelectedSlot(DLSlot slot) {
        this.clickedSlot = slot;
    }

    public DLSlot getSelectedSlot() {
        return clickedSlot;
    }

    public DLSlot getSnapbackEnd() {
        return snapbackEnd;
    }

    public void setSnapbackEnd(DLSlot snapbackEnd) {
        this.snapbackEnd = snapbackEnd;
    }

    public int getSnapbackStartX() {
        return snapbackStartX;
    }

    public void setSnapbackStartX(int snapbackStartX) {
        this.snapbackStartX = snapbackStartX;
    }

    public int getSnapbackStartY() {
        return snapbackStartY;
    }

    public void setSnapbackStartY(int snapbackStartY) {
        this.snapbackStartY = snapbackStartY;
    }

    public long getSnapbackTime() {
        return snapbackTime;
    }

    public void setSnapbackTime(long snapbackTime) {
        this.snapbackTime = snapbackTime;
    }

    public ItemStack getSnapbackItem() {
        return snapbackItem;
    }

    public void setSnapbackItem(ItemStack snapbackItem) {
        this.snapbackItem = snapbackItem;
    }

    public Slot getQuickdropSlot() {
        return quickdropSlot;
    }

    public void setQuickdropSlot(Slot quickdropSlot) {
        this.quickdropSlot = quickdropSlot;
    }

    public long getQuickdropTime() {
        return quickdropTime;
    }

    public void setQuickdropTime(long quickdropTime) {
        this.quickdropTime = quickdropTime;
    }

    @Override
    public void render(Phase phase, DLGuiGraphics graphics, int mouseX, int mouseY, RenderLayer layer) {
        if (phase != Phase.POST && layer != RenderLayer.FRONT) {
            return;
        }
        ItemStack itemStack = getDraggingItem().isEmpty() ? getHoldingItem() : getDraggingItem();
        String string = null;

        if (!itemStack.isEmpty()) {
            if (isQuickCrafting() && getQuickCraftingSlots().size() > 1) {
                itemStack = itemStack.copyWithCount(getQuickCraftingRemainder());
                if (itemStack.isEmpty()) {
                    string = ChatFormatting.YELLOW + "0";
                }
            }
            
            else if (isSplittingStack() && !itemStack.isEmpty()) {
                itemStack = itemStack.copyWithCount(Mth.ceil((float) itemStack.getCount() / 2.0F));
            }

            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0.0F, 0.0F, 232.0F);
            
            int itemX = mouseX - 8;
            int itemY = mouseY - 8;            
            if (!getDraggingItem().isEmpty()) {
                itemY = mouseY - 16;
            }
            GuiUtils.renderItem(graphics, itemStack, itemX, itemY, 1, true);
            GuiUtils.renderItemDecoration(graphics, itemStack, itemX, itemY, 1, string);            
            
            graphics.poseStack().popPose();
        }
        
        if (!this.snapbackItem.isEmpty()) {
            float f = (float) (Util.getMillis() - this.snapbackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }
            
            int l = (int)(this.snapbackEnd.getXOnScreen() - this.snapbackStartX);
            int m = (int)(this.snapbackEnd.getYOnScreen() - this.snapbackStartY);
            int o = this.snapbackStartX + (int) ((float) l * f);
            int p = this.snapbackStartY + (int) ((float) m * f);
            this.renderFloatingItem(graphics, this.snapbackItem, o, p, (String) null);
        }
    }


    private void renderFloatingItem(DLGuiGraphics guiGraphics, ItemStack stack, int x, int y, String text) {
        guiGraphics.poseStack().pushPose();
        guiGraphics.poseStack().translate(0.0F, 0.0F, 232.0F);
        GuiUtils.renderItem(guiGraphics, stack, x, y, 1, false);
        guiGraphics.poseStack().popPose();
    }


}