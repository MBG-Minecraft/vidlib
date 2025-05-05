package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record ReplaceAllSectionBlocks(SectionPos pos, BlockState state) implements BulkLevelModification {
	public static final SimpleRegistryType<ReplaceAllSectionBlocks> TYPE = SimpleRegistryType.dynamic(VidLib.id("section_all_blocks"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		VLCodecs.SECTION_POS.fieldOf("pos").forGetter(ReplaceAllSectionBlocks::pos),
		VLCodecs.BLOCK_STATE.fieldOf("state").forGetter(ReplaceAllSectionBlocks::state)
	).apply(instance, ReplaceAllSectionBlocks::new)), CompositeStreamCodec.of(
		VLStreamCodecs.SECTION_POS, ReplaceAllSectionBlocks::pos,
		VLStreamCodecs.BLOCK_STATE, ReplaceAllSectionBlocks::state,
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
