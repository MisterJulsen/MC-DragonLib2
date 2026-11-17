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

package de.mrjulsen.mcdragonlib.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.resources.ResourceLocation;

@Deprecated
public record DLGuiGraphics(GuiGraphics graphics, PoseStack poseStack, Font defaultFont, float partialTick) {

    public BufferSource bufferSource() {
        return  graphics().bufferSource();
    }

    public VertexConsumer vertexConsumer(ResourceLocation textureLocation) {
        return graphics().bufferSource().getBuffer(RenderType.text(textureLocation));
    }

    public VertexConsumer vertexConsumer() {
        return graphics().bufferSource().getBuffer(RenderType.text(RenderUtils.BLANK_TEXTURE_LOCATION));
    }
}
