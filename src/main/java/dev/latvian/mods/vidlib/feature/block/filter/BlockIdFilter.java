package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockIdFilter(Block block) implements BlockFilter {
	public static final SimpleRegistryType<BlockIdFilter> TYPE = SimpleRegistryType.dynamic("block", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockIdFilter::block)
	).apply(instance, BlockIdFilter::new)), KLibStreamCodecs.registry(BuiltInRegistries.BLOCK).map(BlockIdFilter::new, BlockIdFilter::block));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld b) {
		return b.getState().is(block);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state.is(block);
	}
}
