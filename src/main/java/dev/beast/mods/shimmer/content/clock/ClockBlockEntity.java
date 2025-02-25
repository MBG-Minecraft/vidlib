package dev.beast.mods.shimmer.content.clock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ClockBlockEntity extends BlockEntity {
	public static int prevTicks = 0;
	public static int ticks = 0;
	public static int delta = 0;
	public static final int MAX_TICKS = 60 * 60 * 20 - 1;

	public static void update(int t, int d) {
		prevTicks = ticks = Mth.clamp(t + 19, 0, MAX_TICKS);
		delta = d;
	}

	public static void tick() {
		prevTicks = ticks;
		ticks = Mth.clamp(ticks + delta, 0, MAX_TICKS);
	}

	public String format = "%02d:%02d";

	public ClockBlockEntity(BlockPos pos, BlockState blockState) {
		super(ClockContent.BLOCK_ENTITY.get(), pos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putString("format", format);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		format = tag.getString("format");
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}
}
