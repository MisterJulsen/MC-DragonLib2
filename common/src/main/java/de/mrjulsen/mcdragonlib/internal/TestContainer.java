package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLVerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ScrollableWidgetContainer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class TestContainer extends ScrollableWidgetContainer {

    public TestContainer(int x, int y, int width, int height) {
        super(x, y, width, height);

        for (int k = 0; k < 20; k++) {
            final int n = k;
            DLButton btn = addRenderableWidget(new DLButton(x, y + (k * 20), width - 8, 20, TextUtils.text("Button " + n)));
            btn.setRenderStyle(AreaStyle.DRAGONLIB);
            

            btn.setMenu(createMenu(btn, n));
        }
        
        addRenderableWidget(new DLVerticalScrollBar(x + width - 10, y, 10, height, new GuiAreaDefinition(x, y, width, height)))
            .setAutoScrollerSize(true)
            .setScreenSize(height)
            .setStepSize(15)
            .updateMaxScroll(20 * 20)
        ;
    }

    private DLContextMenu createMenu(DLButton btn, int x) {
        final int n = x;
        DLContextMenu menu = new DLContextMenu(() -> GuiAreaDefinition.of(btn), () -> {
            DLContextMenuItem.Builder builder = new DLContextMenuItem.Builder();
            for (int i = 0; i < 5; i++) {
                builder.add(new ContextMenuItemData(TextUtils.text("Test Item " + n + ": " + i), Sprite.empty(), i != 1, (b) -> {
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
        return menu;
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
    
    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return true;
    }
    
}
