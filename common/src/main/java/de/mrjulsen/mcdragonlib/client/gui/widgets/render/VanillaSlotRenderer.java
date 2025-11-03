package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.container.DLSlot.SlotState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;

public class VanillaSlotRenderer implements ILayeredStateRenderer<SlotState> {

    public static final VanillaSlotRenderer VANILLA_SLOT = new VanillaSlotRenderer();
    
    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, SlotState state) {
        AbstractSprite sprite = switch (state) {
            case SELECTED -> DefaultGuiTextures.DRAGONLIB_UI.getSprite("slot");
            case DISABLED -> DefaultGuiTextures.DRAGONLIB_UI.getSprite("slot");
            case DISABLED_SELECTED -> DefaultGuiTextures.DRAGONLIB_UI.getSprite("slot");
            default -> DefaultGuiTextures.DRAGONLIB_UI.getSprite("slot");
        };
        sprite.render(graphics, x, y, w, h);
    }

    @Override
    public void renderSpritePost(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, SlotState state) {
        switch (state) {
            case SELECTED -> GuiUtils.fill(graphics, 1, 1, 16, 16, DLColor.fromInt(-2130706433));
            default -> {}
        };
    }
}
