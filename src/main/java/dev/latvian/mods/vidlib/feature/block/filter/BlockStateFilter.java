package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockStateFilter(BlockState blockState) implements BlockFilter {
	public static final SimpleRegistryType<BlockStateFilter> TYPE = SimpleRegistryType.dynamic(VidLib.id("block_state"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockState.CODEC.fieldOf("block_state").forGetter(BlockStateFilter::blockState)
	).apply(instance, BlockStateFilter::new)), VLStreamCodecs.BLOCK_STATE.map(BlockStateFilter::new, BlockStateFilter::blockState));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return block.getState() == blockState;
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state == blockState;
	}
}
