package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockXorFilter(BlockFilter a, BlockFilter b) implements BlockFilter {
	public static SimpleRegistryType<BlockXorFilter> TYPE = SimpleRegistryType.dynamic("xor", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.fieldOf("a").forGetter(BlockXorFilter::a),
		BlockFilter.CODEC.fieldOf("b").forGetter(BlockXorFilter::b)
	).apply(instance, BlockXorFilter::new)), CompositeStreamCodec.of(
		BlockFilter.STREAM_CODEC, BlockXorFilter::a,
		BlockFilter.STREAM_CODEC, BlockXorFilter::b,
		BlockXorFilter::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return a.test(block) ^ b.test(block);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return a.test(level, pos, state) ^ b.test(level, pos, state);
	}
}
