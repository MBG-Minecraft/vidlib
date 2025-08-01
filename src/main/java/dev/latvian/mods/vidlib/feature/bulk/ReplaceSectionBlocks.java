package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public record ReplaceSectionBlocks(SectionPos pos, List<BlockPalette> palettes) implements BulkLevelModification {
	public static final SimpleRegistryType<ReplaceSectionBlocks> TYPE = SimpleRegistryType.dynamic("section_blocks", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.SECTION_POS.fieldOf("pos").forGetter(ReplaceSectionBlocks::pos),
		BlockPalette.CODEC.listOf().fieldOf("palettes").forGetter(ReplaceSectionBlocks::palettes)
	).apply(instance, ReplaceSectionBlocks::new)), CompositeStreamCodec.of(
		MCStreamCodecs.SECTION_POS, ReplaceSectionBlocks::pos,
		KLibStreamCodecs.listOf(BlockPalette.STREAM_CODEC), ReplaceSectionBlocks::palettes,
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
