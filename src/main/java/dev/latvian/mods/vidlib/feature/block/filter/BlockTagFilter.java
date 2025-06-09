package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockTagFilter(TagKey<Block> tag) implements BlockFilter {
	public static final SimpleRegistryType<BlockTagFilter> TYPE = SimpleRegistryType.dynamic("tag", RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(BlockTagFilter::tag)
	).apply(instance, BlockTagFilter::new)), KLibStreamCodecs.tagKey(Registries.BLOCK).map(BlockTagFilter::new, BlockTagFilter::tag));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld b) {
		return b.getState().is(tag);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state.is(tag);
	}
}
