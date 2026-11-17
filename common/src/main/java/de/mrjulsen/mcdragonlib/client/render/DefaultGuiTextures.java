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

package de.mrjulsen.mcdragonlib.client.render;

import java.io.IOException;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

@Deprecated(forRemoval = true)
public class DefaultGuiTextures {

    @Deprecated(forRemoval = true)
    public static final DefaultGuiTextures VANILLA_BUTTON = new DefaultGuiTextures(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_buttons.png"));
    @Deprecated(forRemoval = true)
    public static final DefaultGuiTextures DRAGONLIB_UI = new DefaultGuiTextures(new ResourceLocation(DragonLib.MODID, "textures/gui/ui2.png"));
    @Deprecated(forRemoval = true)
    public static final DefaultGuiTextures VANILLA_TEXTBOX = new DefaultGuiTextures(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_textbox.png"));
    @Deprecated(forRemoval = true)
    public static final DefaultGuiTextures VANILLA_SCROLLBAR = new DefaultGuiTextures(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_scrollbar.png"));

    @Deprecated(forRemoval = true)
    public static final String SPRITE_NAME_WINDOW_ROUNDED = "window_rounded";
    
    private final ResourceLocation location;
    private final Supplier<GLGuiTextureData> metadata = Suppliers.memoize(() -> {
        return Minecraft.getInstance().getResourceManager().getResource(location())
            .map(r -> {
                try {
                    return r.metadata().getSection(GLGuiTextureData.SERIALIZER).map(t -> {
                        t.setTextureLocation(location());
                        return t;
                    }).orElse(GLGuiTextureData.EMPTY);
                } catch (IOException e) {
                    return GLGuiTextureData.EMPTY;
                }
            })
            .orElse(GLGuiTextureData.EMPTY);
    });

    @Deprecated(forRemoval = true)
    public DefaultGuiTextures(ResourceLocation location) {
        this.location = location;
    }

    @Deprecated(forRemoval = true)
    public ResourceLocation location() {
        return location;
    }

    @Deprecated(forRemoval = true)
    public GLGuiTextureData metadata() {
        return metadata.get();
    }

    @Deprecated(forRemoval = true)
    public GLGuiTextureData.AbstractSprite getSprite(String spriteName) {
        return metadata().getSprite(spriteName);
    } 
}
