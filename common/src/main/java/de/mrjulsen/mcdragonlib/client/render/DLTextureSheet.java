/*
 * DragonLib 3 - 1.21.1
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
import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData;
import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class DLTextureSheet {

    public static final DLTextureSheet VANILLA_BUTTON = new DLTextureSheet(ResourceLocation.fromNamespaceAndPath(DragonLib.MODID, "textures/gui/vanilla_buttons.png"));
    public static final DLTextureSheet CHECKBOX = new DLTextureSheet(ResourceLocation.fromNamespaceAndPath(DragonLib.MODID, "textures/gui/checkbox.png"));
    public static final DLTextureSheet DRAGONLIB_UI = new DLTextureSheet(ResourceLocation.fromNamespaceAndPath(DragonLib.MODID, "textures/gui/ui2.png"));
    public static final DLTextureSheet VANILLA_TEXTBOX = new DLTextureSheet(ResourceLocation.fromNamespaceAndPath(DragonLib.MODID, "textures/gui/vanilla_textbox.png"));
    public static final DLTextureSheet VANILLA_SCROLLBAR = new DLTextureSheet(ResourceLocation.fromNamespaceAndPath(DragonLib.MODID, "textures/gui/vanilla_scrollbar.png"));

    public static final String SPRITE_NAME_WINDOW_ROUNDED = "window_rounded";
    
    private final ResourceLocation location;
    private final Supplier<DLTextureSheetData> metadata = Suppliers.memoize(() -> {
        return Minecraft.getInstance().getResourceManager().getResource(location())
            .map(r -> {
                try {
                    return r.metadata().getSection(DLTextureSheetData.SERIALIZER).map(t -> {
                        t.setTextureLocation(location());
                        return t;
                    }).orElse(DLTextureSheetData.EMPTY);
                } catch (IOException e) {
                    return DLTextureSheetData.EMPTY;
                }
            })
            .orElse(DLTextureSheetData.EMPTY);
    });

    public DLTextureSheet(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation location() {
        return location;
    }

    public DLTextureSheetData metadata() {
        return metadata.get();
    }

    public AbstractSprite getSprite(String spriteName) {
        return metadata().getSprite(spriteName);
    } 
}
