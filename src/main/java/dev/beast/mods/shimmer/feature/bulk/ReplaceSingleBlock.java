package dev.beast.mods.shimmer.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record ReplaceSingleBlock(BlockPos pos, BlockState state) implements BulkLevelModification {
	public static final SimpleRegistryType<ReplaceSingleBlock> TYPE = SimpleRegistryType.dynamic(Shimmer.id("block"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("pos").forGetter(ReplaceSingleBlock::pos),
		BlockState.CODEC.fieldOf("state").forGetter(ReplaceSingleBlock::state)
	).apply(instance, ReplaceSingleBlock::new)), CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, ReplaceSingleBlock::pos,
		ShimmerStreamCodecs.BLOCK_STATE, ReplaceSingleBlock::state,
		ReplaceSingleBlock::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public void collectSections(Level level, Set<SectionPos> sections) {
		sections.add(SectionPos.of(pos));
	}

	@Override
	public void apply(BlockModificationConsumer blocks) {
		blocks.set(pos, state);
	}
}
