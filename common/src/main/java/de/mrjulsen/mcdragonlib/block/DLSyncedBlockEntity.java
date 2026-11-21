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

package de.mrjulsen.mcdragonlib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public abstract class DLSyncedBlockEntity extends BlockEntity {

	public DLSyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithFullMetadata(registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	// Special handling for client update packets
	public void readClient(CompoundTag tag) {
		loadAdditional(tag, registryAccess());
	}

	// Special handling for client update packets
	public CompoundTag writeClient(CompoundTag tag) {
		saveAdditional(tag, registryAccess());
		return tag;
	}

	private HolderLookup.Provider registryAccess() {
		return level != null ? level.registryAccess() : RegistryAccess.EMPTY;
	}

	public void sendData() {
		if (level instanceof ServerLevel serverLevel) {			
			serverLevel.getChunkSource().blockChanged(getBlockPos());
		}
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

	public LevelChunk containedChunk() {
		return level.getChunkAt(worldPosition);
	}
}
