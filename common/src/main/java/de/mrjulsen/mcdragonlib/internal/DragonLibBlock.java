package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DragonLibBlock extends Block {

    public DragonLibBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (Platform.isDevelopmentEnvironment() && level.isClientSide()) {
            ClientWrapper.openTestScreen();
        }
        return InteractionResult.SUCCESS;
    }

    public static class DragonLibItem extends BlockItem {
        public DragonLibItem(Block pBlock, Properties pProperties) {
            super(pBlock, pProperties.rarity(Rarity.EPIC));            
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
            return InteractionResultHolder.success(player.getItemInHand(interactionHand));
        }
    }
    
}