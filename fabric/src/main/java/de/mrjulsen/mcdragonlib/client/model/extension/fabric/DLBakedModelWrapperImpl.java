package de.mrjulsen.mcdragonlib.client.model.extension.fabric;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class DLBakedModelWrapperImpl extends ForwardingBakedModel implements FabricBakedModel {

    public DLBakedModelWrapperImpl(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
        context.pushTransform(new EmissiveQuadTransform());
        super.emitBlockQuads(level, state, pos, random, context);
        context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
        context.pushTransform(new EmissiveQuadTransform());
        super.emitItemQuads(stack, random, context);
        context.popTransform();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    public static BakedModel wrap(BakedModel original) {
        return new DLBakedModelWrapperImpl(original);
    }

}