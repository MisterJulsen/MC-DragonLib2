package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.BERLabel;
import de.mrjulsen.mcdragonlib.client.ber.BasicBlockEntityRenderer;
import de.mrjulsen.mcdragonlib.client.model.mesh.CornerType;
import de.mrjulsen.mcdragonlib.client.model.mesh.CubeMesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import de.mrjulsen.mcdragonlib.client.util.RenderUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class DragonLibBlockEntityRenderer extends BasicBlockEntityRenderer<DragonLibBlockEntity> {
    //private final BERLabel label = new BERLabel();
    //private final Mesh model;

    public DragonLibBlockEntityRenderer(Context context) {
        super(context);
        /*
        label.text.set(TextUtils.text("DragonLib").withStyle(ChatFormatting.GOLD));
        label.y.set(11f);
        label.fullBackground.set(false);
        label.horizontalScrollingSpeed.set(8f);
        label.horizontalAlign.set(ETextAlignment.LEFT);
        label.horizontalMaxScale.set(0.5f);
        label.horizontalMinScale.set(0.5f);
        label.verticalMaxScale.set(0.5f);
        label.verticalMinScale.set(0.5f);
        label.horizontalScrollMode.set(BERLabel.EScrollMode.ALWAYS);

        //model = BasicMesh.fromBlock(Blocks.MAGENTA_GLAZED_TERRACOTTA.defaultBlockState(), RandomSource.create());

        model = new CubeMesh(new Vector3f(), new Vector3f(1, 1, 1));
        model.getFacesOfDirection(Direction.NORTH).forEach(f -> {
            f.getCorner(CornerType.TOP_RIGHT).setU(0.5f);
            f.setTexture(new ResourceLocation("textures/block/grass_block_side.png"));
        });
         */
    }


    @Override
    protected void renderBlock(BERGraphics<DragonLibBlockEntity> graphics, float partialTick) {
        /*
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, 16.01f);
        //model.render(graphics, graphics.packedLight(), true);
        //RenderUtils.drawString(graphics, font, 0, 0, "DragonLib", DLColor.WHITE, ETextAlignment.LEFT, false);
        //RenderUtils.renderTexture(DLUtils.resourceLocation("textures/block/crafting_table_front.png"), graphics, new Vector3f(), 1, 1, Direction.EAST,true);
        //label.render(graphics);
        graphics.poseStack().popPose();

         */
    }
}
