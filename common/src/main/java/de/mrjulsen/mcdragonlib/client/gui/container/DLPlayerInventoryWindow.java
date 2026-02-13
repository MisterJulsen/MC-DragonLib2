package de.mrjulsen.mcdragonlib.client.gui.container;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLMenuWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.FlatButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.menu.PlayerInventoryContainerMenu;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;

public class DLPlayerInventoryWindow extends DLMenuWindow<PlayerInventoryContainerMenu> {

    private static final int HEADER_SIZE = 22;
    private static final int BUTTON_BORDER_DISTANCE = 5;
    private static final int BORDER = 6;

    public DLPlayerInventoryWindow(DLWindowManager manager) {
        super(PlayerInventoryContainerMenu.class, manager);
        movable.set(true);
        windowSpawnPosition.set(WindowPosition.CENTER);

        DLPlayerInventoryComponent<PlayerInventoryContainerMenu> inventory = new DLPlayerInventoryComponent<>(BORDER, HEADER_SIZE, menu);
        addComponent(inventory);
        setSize(inventory.width() + BORDER * 2, inventory.height() + HEADER_SIZE + BORDER);
        
        DLButton closeBtn = new DLButton(width() - BUTTON_BORDER_DISTANCE - (HEADER_SIZE - BUTTON_BORDER_DISTANCE - 1), BUTTON_BORDER_DISTANCE, HEADER_SIZE - BUTTON_BORDER_DISTANCE - 1, HEADER_SIZE - BUTTON_BORDER_DISTANCE - 1);
        closeBtn.text.set(TextUtils.text("×"));
        closeBtn.textColor.set(DragonLib.VANILLA_UI_FONT_COLOR);
        closeBtn.drawFontShadow.set(false);
        closeBtn.anchor.set2(EAlign.RIGHT, EAlign.TOP);
        closeBtn.componentRenderer.set(FlatButtonRenderer.INSTANCE);
        closeBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });
        addComponent(closeBtn);

    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, BORDER, 1 + HEADER_SIZE / 2 - Minecraft.getInstance().font.lineHeight / 2, TextUtils.text("Inventory"), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }
    
}
