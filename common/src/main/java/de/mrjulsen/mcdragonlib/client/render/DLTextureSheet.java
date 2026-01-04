package de.mrjulsen.mcdragonlib.client.render;

import java.io.IOException;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData;
import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class DLTextureSheet {

    public static final DLTextureSheet VANILLA_BUTTON = new DLTextureSheet(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_buttons.png"));
    public static final DLTextureSheet DRAGONLIB_UI = new DLTextureSheet(new ResourceLocation(DragonLib.MODID, "textures/gui/ui2.png"));
    public static final DLTextureSheet VANILLA_TEXTBOX = new DLTextureSheet(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_textbox.png"));
    public static final DLTextureSheet VANILLA_SCROLLBAR = new DLTextureSheet(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_scrollbar.png"));

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
