package dev.beast.mods.shimmer.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record ReplaceAllSectionBlocks(SectionPos pos, BlockState state) implements BulkLevelModification {
	public static final SimpleRegistryType<ReplaceAllSectionBlocks> TYPE = SimpleRegistryType.dynamic(Shimmer.id("section_all_blocks"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ShimmerCodecs.SECTION_POS.fieldOf("pos").forGetter(ReplaceAllSectionBlocks::pos),
		BlockState.CODEC.fieldOf("state").forGetter(ReplaceAllSectionBlocks::state)
	).apply(instance, ReplaceAllSectionBlocks::new)), CompositeStreamCodec.of(
		ShimmerStreamCodecs.SECTION_POS, ReplaceAllSectionBlocks::pos,
		ShimmerStreamCodecs.BLOCK_STATE, ReplaceAllSectionBlocks::state,
		ReplaceAllSectionBlocks::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public void collectSections(Level level, Set<SectionPos> sections) {
		sections.add(pos);
	}

	@Override
	public void apply(BlockModificationConsumer blocks) {
		blocks.fillSection(pos, state);
	}
}
