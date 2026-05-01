package de.mrjulsen.mcdragonlib.client.util;

import java.io.IOException;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData;
import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class GuiTexture {

    public static final GuiTexture VANILLA_BUTTON = new GuiTexture(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_buttons.png"));
    public static final GuiTexture DRAGONLIB_UI = new GuiTexture(new ResourceLocation(DragonLib.MODID, "textures/gui/ui2.png"));
    public static final GuiTexture VANILLA_TEXTBOX = new GuiTexture(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_textbox.png"));
    public static final GuiTexture VANILLA_SCROLLBAR = new GuiTexture(new ResourceLocation(DragonLib.MODID, "textures/gui/vanilla_scrollbar.png"));
    
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

    public GuiTexture(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation location() {
        return location;
    }

    public GLGuiTextureData metadata() {
        return metadata.get();
    }

    public AbstractSprite getSprite(String spriteName) {
        return metadata().getSprite(spriteName);
    } 
}
