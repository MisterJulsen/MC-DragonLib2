/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.client.gui.v2.widget;

import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.network.chat.Component;

public class DLButton extends DLAbstractButton {

    public final Property<Component> text = new Property<>(TextUtils.text(getClass().getSimpleName()));
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
