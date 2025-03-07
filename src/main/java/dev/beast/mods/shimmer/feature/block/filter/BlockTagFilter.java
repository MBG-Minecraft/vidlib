package dev.beast.mods.shimmer.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockTagFilter(TagKey<Block> tag) implements BlockFilter {
	public static final SimpleRegistryType<BlockTagFilter> TYPE = SimpleRegistryType.dynamic(Shimmer.id("tag"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(BlockTagFilter::tag)
	).apply(instance, BlockTagFilter::new)), ShimmerStreamCodecs.tagKey(Registries.BLOCK).map(BlockTagFilter::new, BlockTagFilter::tag));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld b) {
		return b.getState().is(tag);
	}
}
