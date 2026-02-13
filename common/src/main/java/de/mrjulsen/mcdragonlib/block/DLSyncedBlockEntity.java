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
		loadAdditional(tag, RegistryAccess.EMPTY);
	}

	// Special handling for client update packets
	public CompoundTag writeClient(CompoundTag tag) {
		saveAdditional(tag, RegistryAccess.EMPTY);
		return tag;
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
