package de.mrjulsen.mcdragonlib.client.gui.v2.widget;

import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.WidgetState;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class DLToggleButton extends DLAbstractToggleButton {

    public static class Renderer extends DLAbstractButton.Renderer {

        public Renderer(DLTextureSheet spriteSheet) {
            super(spriteSheet);
        }

        @Override
        protected String getSprite(DLAbstractButton component) {
            if (component.enabled.get() && component instanceof DLToggleButton t && t.checked.get()) {
                if (component.isHovered() || component.isFocused()) {
                    return SPRITE_DOWN_SELECTED;
                } else {
                    return SPRITE_DOWN;
                }
            }
            return super.getSprite(component);
        }
    }

    public static class FlatRenderer extends Renderer {

        private final String spritePrefix;

        public FlatRenderer(DLTextureSheet spriteSheet, String spritePrefix) {
            super(spriteSheet);
            this.spritePrefix = spritePrefix;
        }

        @Override
        protected String getSprite(DLAbstractButton component) {
            return "button_" + spritePrefix + "_" + switch (WidgetState.of(component)) {
                case DISABLED, DISABLED_HOVERED -> SPRITE_DISABLED;
                case HOVERED -> SPRITE_SELECTED;
                case PRESSED -> SPRITE_DOWN_SELECTED;
                default -> SPRITE_NORMAL;
            };
        }
    }

    public static final DLAbstractButton.Renderer VANILLA_RENDERER = new Renderer(DLTextureSheet.VANILLA_BUTTON);
    public static final DLAbstractButton.Renderer VANILLA_LEGACY_RENDERER = new Renderer(DLTextureSheet.VANILLA_BUTTON) {
        @Override
        protected String getSprite(DLAbstractButton component) {
            return "legacy_" + super.getSprite(component);
        }
    };
    public static final DLAbstractButton.Renderer FLAT_GRAY_RENDERER = new FlatRenderer(DLTextureSheet.DRAGONLIB_UI, "gray");
    public static final DLAbstractButton.Renderer FLAT_BROWN_RENDERER = new FlatRenderer(DLTextureSheet.DRAGONLIB_UI, "brown");
    public static final DLAbstractButton.Renderer FLAT_RED_RENDERER = new FlatRenderer(DLTextureSheet.DRAGONLIB_UI, "red");

    public final Property<Component> text = new Property<>(TextUtils.text(getClass().getSimpleName()));
    public final Property<Function<Boolean, DLSprite>> icon = new Property<>((state -> null));

    public DLToggleButton(int x, int y, int w, int h) {
        super(x, y, w, h);

        overrideThemeKey(THEME_KEY_RENDERER, VANILLA_RENDERER);
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
