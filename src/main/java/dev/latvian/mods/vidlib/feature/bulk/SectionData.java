package dev.latvian.mods.vidlib.feature.bulk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.Arrays;

public class SectionData {
	public final SectionPos pos;
	private final BlockState[] states;
	public LevelChunk levelChunk;
	public LevelChunkSection levelChunkSection;

	public boolean modified;
	public boolean hasOnlyAir;

	public SectionData(SectionPos pos) {
		this.pos = pos;
		this.states = new BlockState[4096];
		this.modified = false;
		this.hasOnlyAir = true;
	}

	public static int index(int x, int y, int z) {
		return ((y & 15) << 4 | (z & 15)) << 4 | (x & 15);
	}

	public static int index(BlockPos pos) {
		return index(pos.getX(), pos.getY(), pos.getZ());
	}

	public static void getIndex(BlockPos.MutableBlockPos pos, SectionPos sectionPos, int index) {
		pos.set(sectionPos.minBlockX() + (index & 15), sectionPos.minBlockY() + ((index >> 4) & 15), sectionPos.minBlockZ() + ((index >> 8) & 15));
	}

	public void fill(BlockState state) {
		Arrays.fill(states, state);
		modified = true;

		if (!state.isAir()) {
			hasOnlyAir = false;
		}
	}

	public void setBlock(int index, BlockState state) {
		states[index] = state;
		modified = true;

		if (!state.isAir()) {
			hasOnlyAir = false;
		}
	}

	public void setBlock(int x, int y, int z, BlockState state) {
		setBlock(index(x, y, z), state);
	}

	public BlockState getBlock(int x, int y, int z) {
		return states[index(x, y, z)];
	}
}
