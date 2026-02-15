package de.mrjulsen.mcdragonlib.internal;

import com.mojang.serialization.MapCodec;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.menu.PlayerInventoryContainerMenu;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DragonLibBlock extends BaseEntityBlock {

    public static final MapCodec<DragonLibBlock> CODEC = simpleCodec(DragonLibBlock::new);

    public DragonLibBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static class DragonLibItem extends BlockItem {
        public DragonLibItem(Block pBlock, Properties pProperties) {
            super(pBlock, pProperties.rarity(Rarity.EPIC));            
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
        return new DragonLibBlockEntity(arg0, arg1);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            //DLWindow.openWindow(mgr -> new DLTestWindow(mgr));
            //DLOverlayManager.addOverlay(mgr -> new TimeWindow(mgr));

            /*
            NetworkTest.SEND_AND_RECEIVE.send(NetworkDirection.toServer(), new NetworkTest.TestData(DLStatus.OK, "Salzingen Hbf"), (response) -> {
                DragonLib.LOGGER.info("Response: " + response.txt);
            }, () -> {});

             */
            //NetworkTest.SEND.send(NetworkDirection.toServer(), new NetworkTest.TestData(DLStatus.OK, "Salzingen Hbf"));
            return InteractionResult.SUCCESS;
        } else {
            //pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            return InteractionResult.CONSUME;
        }
    }

    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider((containerId, inv, player) -> {
            return new PlayerInventoryContainerMenu.Base(containerId, inv, ContainerLevelAccess.create(pLevel, pPos));
        }, TextUtils.text(""));
    }
    
}