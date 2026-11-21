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

package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.BERLabel;
import de.mrjulsen.mcdragonlib.client.ber.BasicBlockEntityRenderer;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import dev.architectury.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class DragonLibBlockEntityRenderer extends BasicBlockEntityRenderer<DragonLibBlockEntity> {
    private final BERLabel label = new BERLabel();
    //private final Mesh model;

    public DragonLibBlockEntityRenderer(Context context) {
        super(context);
        label.text.set(TextUtils.text("DragonLib").withStyle(ChatFormatting.GOLD).append(" ").append("🐉"));
        label.y.set(11f);
        label.fullBackground.set(false);
        label.horizontalScrollingSpeed.set(8f);
        label.horizontalAlign.set(ETextAlignment.LEFT);
        label.horizontalMaxScale.set(0.1f);
        label.horizontalMinScale.set(0.1f);
        label.verticalMaxScale.set(0.1f);
        label.verticalMinScale.set(0.1f);
        label.clippingArea.set(Rectangle.withSize(0, 0, 8, 16));
        label.horizontalScrollMode.set(BERLabel.EScrollMode.ALWAYS);
        label.glowing.set(true);
        label.shadow.set(true);
        label.scrollPause.set(20f);

        /*
        model = BasicMesh.fromBlock(Blocks.MAGENTA_GLAZED_TERRACOTTA.defaultBlockState(), RandomSource.create());

        model = new CubeMesh(new Vector3f(), new Vector3f(1, 1, 1));
        model.getFacesOfDirection(Direction.NORTH).forEach(f -> {
            f.getCorner(CornerType.TOP_RIGHT).setU(0.5f);
            f.setTexture(ResourceLocation.parse("textures/block/grass_block_side.png"));
        });

         */
    }


    @Override
    protected void renderBlock(BERGraphics<DragonLibBlockEntity> graphics, float partialTick) {
        if (!Platform.isDevelopmentEnvironment()) {
            return;
        }

        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, 16.01f);
        //model.render(graphics, graphics.packedLight(), true);
        //RenderUtils.drawString(graphics, font, 0, 0, "DragonLib", DLColor.WHITE, ETextAlignment.LEFT, false);
        //RenderUtils.renderTexture(DLUtils.resourceLocation("textures/block/crafting_table_front.png"), graphics, new Vector3f(), 1, 1, Direction.EAST,true);
        label.render(graphics);
        graphics.poseStack().popPose();
    }
}
