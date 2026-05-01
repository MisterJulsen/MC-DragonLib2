package de.mrjulsen.mcdragonlib.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

@Mixin(Font.class)
public interface FontAccessor {

    @Invoker("getFontSet")
    public FontSet dragonlib$invokeGetFontSet(ResourceLocation pFontLocation);

    @Accessor("splitter")
    StringSplitter dragonlib$getSplitter();

    @Accessor("filterFishyGlyphs")
    boolean dragonlib$filterFishyGlyphs();

    @Invoker("renderChar")
    void dragonlib$renderChar(BakedGlyph glyph, boolean bold, boolean italic, float boldOffset, float x, float y, Matrix4f matrix, VertexConsumer buffer, float red, float green, float blue, float alpha, int packedLight);
}

