package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.util.ID;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record ReplaceAllSectionBlocks(SectionPos pos, BlockState state) implements BulkLevelModification {
	public static final CustomRegistryType<ByteBuf, BulkLevelModification> TYPE = REGISTRY.dynamic(
		ID.vidlib("section_all_blocks"),
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			MCCodecs.SECTION_POS.fieldOf("pos").forGetter(ReplaceAllSectionBlocks::pos),
			MCCodecs.BLOCK_STATE.fieldOf("state").forGetter(ReplaceAllSectionBlocks::state)
		).apply(instance, ReplaceAllSectionBlocks::new)), CompositeStreamCodec.of(
			MCStreamCodecs.SECTION_POS, ReplaceAllSectionBlocks::pos,
			MCStreamCodecs.BLOCK_STATE, ReplaceAllSectionBlocks::state,
			ReplaceAllSectionBlocks::new
		)
	);

	@Override
	public CustomRegistryType<ByteBuf, BulkLevelModification> type() {
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
