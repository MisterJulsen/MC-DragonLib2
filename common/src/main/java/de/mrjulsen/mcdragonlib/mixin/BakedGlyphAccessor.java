package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;

@Mixin(BakedGlyph.class)
public interface BakedGlyphAccessor {
    
	@Mutable
    @Accessor("u0")
    void dragonlib$setU0(float value);
	
    @Accessor("u0")
    float dragonlib$getU0();

	@Mutable
    @Accessor("u1")
    void dragonlib$setU1(float value);

    @Accessor("u1")
    float dragonlib$getU1();


    @Mutable
    @Accessor("v0")
    void dragonlib$setV0(float value);

    @Accessor("v0")
    float dragonlib$getV0();

    @Mutable
    @Accessor("v1")
    void dragonlib$setV1(float value);
	
    @Accessor("v1")
    float dragonlib$getV1();

}
