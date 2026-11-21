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

package de.mrjulsen.mcdragonlib.mixin.client;

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
