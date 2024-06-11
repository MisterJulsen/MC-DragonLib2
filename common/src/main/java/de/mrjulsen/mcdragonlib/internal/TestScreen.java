package de.mrjulsen.mcdragonlib.internal;

import java.util.concurrent.atomic.AtomicReference;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLColorPickerScreen;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLCheckBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLDropDownButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLNumberSelector;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSplitButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLVerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TestScreen extends DLScreen {

    protected TestScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        DLButton btn = addButton(50, 50, 100, 20, title, (b) -> {}, null);
        btn.active = false;
        btn.setRenderStyle(AreaStyle.DRAGONLIB);

        addButton(50, 80, 100, 20, title, (b) -> setScreen(new DLColorPickerScreen(this, 0, (c) -> {}, true)), null).setRenderStyle(AreaStyle.DRAGONLIB);
        Sprite sprite = new Sprite(new ResourceLocation(DragonLib.MODID, "textures/gui/icons.png"), 256, 256, 0, 16, 16, 16, 12, 12);

        addRenderableWidget(new DLCheckBox(150, 100, 100, "CheckBox Widget Text", true, (cb) -> {
            System.out.println("Checkbox state is: " + cb.isChecked());
        })).active = false;

        addRenderableWidget(new DLNumberSelector(150, 150, 100, 20, 0, true, (box, itm) -> {}));

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

        DLSplitButton split = addRenderableWidget(new DLSplitButton(50, 140, 100, 20, TextUtils.text("Button 3"), (b) -> Minecraft.getInstance().setScreen(null),
        new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
            DLContextMenuItem.Builder builder2 = new DLContextMenuItem.Builder();
            builder2.add(new ContextMenuItemData(TextUtils.text("Test A"), Sprite.empty(), true, (b) -> {}, null));
            builder2.addSeparator();
            builder2.add(new ContextMenuItemData(TextUtils.text("Test B"), Sprite.empty(), true, (b) -> {}, null));
            return builder2;
        })));
        split.setRenderStyle(AreaStyle.DRAGONLIB);
        split.setBackColor(DragonLib.ERROR_BUTTON_COLOR);

        addRenderableWidget(new DLDropDownButton(50, 170, 100, 20, TextUtils.text("Button 4"),
        new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
            DLContextMenuItem.Builder builder2 = new DLContextMenuItem.Builder();
            builder2.add(new ContextMenuItemData(TextUtils.text("Test"), Sprite.empty(), true, (b) -> {}, null));
            builder2.addSeparator();
            builder2.add(new ContextMenuItemData(TextUtils.text("Close"), Sprite.empty(), true, (b) -> {}, null));
            return builder2;
        })));
        addEditBox(50, 110, 100, 20, "", TextUtils.text("öl"), true, (v) -> {}, (e, b) -> {}, null);
        AtomicReference<TestContainer> container = new AtomicReference<>();
        DLVerticalScrollBar scrollBar = addRenderableWidget(new DLVerticalScrollBar(350, 50, 90, new GuiAreaDefinition(250, 50, 100, 100)));
        scrollBar.setPageSize(90);
        scrollBar.updateMaxScroll(20 * 20);
        scrollBar.setStepSize(8);
        scrollBar.setAutoScrollerHeight(true);
        scrollBar.setOnValueChangedEvent((bar) -> {
            container.get().setYScrollOffset(bar.getScrollValue());
        });
        container.set(addRenderableWidget(new TestContainer(225, 70, 100, 90)));
        container.get().setWidgetLayerIndex(1);

        setAllowedLayer(0);

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
