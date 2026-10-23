package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class DLToggleButton extends DLAbstractToggleButton {

    public static class Renderer extends DLAbstractButton.Renderer {

        public static final Renderer STANDARD = new Renderer(DLTextureSheet.VANILLA_BUTTON);
        public static final String SPRITE_DOWN = "down";

        public Renderer(DLTextureSheet spriteSheet) {
            super(spriteSheet);
        }

        @Override
        public void render(DLGuiGraphics g, GuiComponent component, double x, double y, double width, double height) {
            if (component.enabled.get() && component instanceof DLToggleButton t && t.checked.get()) {
                if (component.isHovered() || component.isFocused()) {
                    GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_PRESSED), x, y, width, height);
                } else {
                    GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_DOWN), x, y, width, height);
                }
            } else {
                super.render(g, component, x, y, width, height);
            }
        }
    }

    public final Property<Component> text = new Property<>(TextUtils.empty());
    public final Property<Function<Boolean, DLSprite>> icon = new Property<>((state -> null));

    public DLToggleButton(int x, int y, int w, int h) {
        super(x, y, w, h);

        overrideThemeKey(THEME_KEY_RENDERER, Renderer.STANDARD);
    }

    @Override
    protected DLSprite getIcon() {
        return icon.get().apply(checked.get());
    }

    @Override
    protected Component getText() {
        return text.get();
    }
}
