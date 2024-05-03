package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLColorPickerScreen;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLCheckBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLDropDownButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLItemButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLNumberSelector;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSplitButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TestScreen extends DLScreen {

    protected TestScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        DLButton btn = addButton(50, 50, 100, 20, title, (b) -> {}, null);
        btn.active = false;
        
        addButton(50, 80, 100, 20, title, (b) -> setScreen(new DLColorPickerScreen(this, 0, (c) -> {})), null);
        Sprite sprite = new Sprite(new ResourceLocation(DragonLib.MODID, "textures/gui/icons.png"), 256, 256, 0, 16, 16, 16, 12, 12);

        addRenderableWidget(new DLCheckBox(170, 100, 100, "CheckBox Widget Text", true, (cb) -> {
            System.out.println("Checkbox state is: " + cb.isChecked());
        })).active = false;
        
        addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.BROWN, sprite, 170, 50, 100, 20, title, (b) -> {}));
        addRenderableWidget(new DLItemButton(ButtonType.DEFAULT, AreaStyle.BROWN, new ItemStack(DragonLib.DRAGON_BLOCK.get()), 170, 75, 100, 20, null, (b) -> {}));

        addRenderableWidget(new DLNumberSelector(170, 150, 100, 20, 0, true, (box, itm) -> {}));
        addRenderableWidget(new DLNumberSelector(170, 175, 100, 20, 0, true, (box, itm) -> {})).setActive(false);

        DLContextMenu menu = new DLContextMenu(() -> {
            return GuiAreaDefinition.of(this);
        }, () -> {
            DLContextMenuItem.Builder builder = new DLContextMenuItem.Builder();
            for (int i = 0; i < 5; i++) {
                builder.add(new ContextMenuItemData(title, sprite, i != 1, (b) -> {
                    Minecraft.getInstance().setScreen(null);
                }, i == 2 ? (parentItem) -> new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
                    DLContextMenuItem.Builder builder2 = new DLContextMenuItem.Builder();
                    builder2.add(new ContextMenuItemData(TextUtils.text("Menu Item 1"), Sprite.empty(), true, (b) -> {}, null));
                    builder2.add(new ContextMenuItemData(TextUtils.text("Menu Item 2"), Sprite.empty(), true, (b) -> {}, null));
                    return builder2;
                }) : null));
            }
            return builder;
        });
        
        setMenu(menu);

        addRenderableWidget(new DLSplitButton(50, 140, 100, 20, TextUtils.text("Button 3"), (b) -> Minecraft.getInstance().setScreen(null),
        new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
            DLContextMenuItem.Builder builder2 = new DLContextMenuItem.Builder();
            builder2.add(new ContextMenuItemData(TextUtils.text("Test A"), Sprite.empty(), true, (b) -> {}, null));
            builder2.addSeparator();
            builder2.add(new ContextMenuItemData(TextUtils.text("Test B"), Sprite.empty(), true, (b) -> {}, null));
            return builder2;
        })));

        addRenderableWidget(new DLDropDownButton(50, 170, 100, 20, TextUtils.text("Button 4"),
        new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
            DLContextMenuItem.Builder builder2 = new DLContextMenuItem.Builder();
            builder2.add(new ContextMenuItemData(TextUtils.text("Test"), Sprite.empty(), true, (b) -> {}, null));
            builder2.addSeparator();
            builder2.add(new ContextMenuItemData(TextUtils.text("Close"), Sprite.empty(), true, (b) -> {}, null));
            return builder2;
        })));
        addEditBox(50, 110, 100, 20, "", TextUtils.text("öl"), true, (v) -> {}, (e, b) -> {}, null);

    }

    @Override
    public void renderBackLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackLayer(graphics, mouseX, mouseY, partialTick);
        renderBackground(graphics.poseStack());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
