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

import com.mojang.serialization.MapCodec;

import de.mrjulsen.mcdragonlib.client.gui.v2.window.DLHudManager;
import de.mrjulsen.mcdragonlib.client.gui.v2.window.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.v2.window.DLWindow;
import de.mrjulsen.mcdragonlib.menu.PlayerInventoryContainerMenu;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
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
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        if (!Platform.isDevelopmentEnvironment()) {
            return InteractionResult.PASS;
        }

        if (pLevel.isClientSide) {
            if (pPlayer.isShiftKeyDown()) {
                DLScreen.open(mgr -> new TestGui());
            } else {
                //DLOverlayManager.addOverlay(mgr -> new TimeWindow(mgr));
                //DLWindow.openWindow(mgr -> new DLTestWindow(mgr));
            }
            return InteractionResult.SUCCESS;
        } else {
            //pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider((containerId, inv, player) -> {
            return new PlayerInventoryContainerMenu.Base(containerId, inv, ContainerLevelAccess.create(pLevel, pPos));
        }, TextUtils.text(""));
    }
    
}