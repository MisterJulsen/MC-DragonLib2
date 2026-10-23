package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.network.chat.Component;

public class DLButton extends DLAbstractButton {

    public final Property<Component> text = new Property<>(TextUtils.empty());
    public final Property<DLSprite> icon = new Property<>(DLSprite.empty());

    public DLButton(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.text.addChangeListener((o, n) -> {
            invalidateLayout();
            invokeEvent(this, new CaptionChangedEvent(n));
        });
    }

    @Override
    public Component getText() {
        return text.get();
    }

    @Override
    public DLSprite getIcon() {
        return icon.get();
    }
}
