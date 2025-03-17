package dev.beast.mods.shimmer.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public record ReplaceSectionBlocks(SectionPos pos, List<BlockPalette> palettes) implements BulkLevelModification {
	public static final SimpleRegistryType<ReplaceSectionBlocks> TYPE = SimpleRegistryType.dynamic(Shimmer.id("section_blocks"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ShimmerCodecs.SECTION_POS.fieldOf("pos").forGetter(ReplaceSectionBlocks::pos),
		BlockPalette.CODEC.listOf().fieldOf("palettes").forGetter(ReplaceSectionBlocks::palettes)
	).apply(instance, ReplaceSectionBlocks::new)), CompositeStreamCodec.of(
		ShimmerStreamCodecs.SECTION_POS, ReplaceSectionBlocks::pos,
		BlockPalette.STREAM_CODEC.list(), ReplaceSectionBlocks::palettes,
		ReplaceSectionBlocks::new
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
		blocks.applyPalettes(pos, palettes);
	}
}
