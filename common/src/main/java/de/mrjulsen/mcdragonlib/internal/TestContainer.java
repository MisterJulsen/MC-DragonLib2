package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ScrollableWidgetContainer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class TestContainer extends ScrollableWidgetContainer {

    public TestContainer(int x, int y, int width, int height) {
        super(x, y, width, height);

        for (int k = 0; k < 20; k++) {
            DLButton btn = addRenderableWidget(new DLButton(x, y + (k * 20), width, 20, TextUtils.text("Button " + k)));
            
            DLContextMenu menu = new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
                DLContextMenuItem.Builder builder = new DLContextMenuItem.Builder();
                for (int i = 0; i < 5; i++) {
                    builder.add(new ContextMenuItemData(TextUtils.text("Test Item " + i), Sprite.empty(), i != 1, (b) -> {
                        System.out.println("Success");
                    }, i == 2 ? (parentItem) -> new DLContextMenu(() -> null, () -> {
                        DLContextMenuItem.Builder builder2 = new DLContextMenuItem.Builder();
                        builder2.add(new ContextMenuItemData(TextUtils.text("Menu Item 1"), Sprite.empty(), true, (b) -> {}, null));
                        builder2.add(new ContextMenuItemData(TextUtils.text("Menu Item 2"), Sprite.empty(), true, (b) -> {}, null));
                        return builder2;
                    }) : null));
                }
                return builder;
            });

            btn.setMenu(menu);
        }

        
    }

    @Override
    public void renderMainLayerScrolled(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        GuiUtils.fill(graphics, 0, 0, width, height, 0xFFFF0000);
        super.renderMainLayerScrolled(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
    
}