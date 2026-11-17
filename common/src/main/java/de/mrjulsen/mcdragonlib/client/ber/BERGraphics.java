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

package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.mrjulsen.mcdragonlib.client.util.DLGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BERGraphics<B extends BlockEntity> extends DLGraphics { 
    
    protected final B blockEntity;
    protected final BlockEntityRendererProvider.Context berProviderContext;

    public BERGraphics(B blockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, BlockEntityRendererProvider.Context berProviderContext, int packedLight, int packedOverlay, float partialTick) {
        super(poseStack, multiBufferSource, packedLight, packedOverlay, partialTick);
        this.blockEntity = blockEntity;
        this.berProviderContext = berProviderContext;
    }
    
    public B blockEntity() {
        return blockEntity;
    }

    public BlockEntityRendererProvider.Context BERProviderContext() {
        return berProviderContext;
    }

    public VertexConsumer vertexConsumer(ResourceLocation textureLocation) {
        return multiBufferSource.getBuffer(RenderType.text(textureLocation));
    }
}
