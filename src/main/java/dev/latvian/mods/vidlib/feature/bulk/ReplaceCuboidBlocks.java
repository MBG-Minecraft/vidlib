package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record ReplaceCuboidBlocks(BlockPos start, BlockPos end, BlockState state) implements BulkLevelModification {
	public static final SimpleRegistryType<ReplaceCuboidBlocks> TYPE = SimpleRegistryType.dynamic("cuboid_blocks", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("start").forGetter(ReplaceCuboidBlocks::start),
		BlockPos.CODEC.fieldOf("end").forGetter(ReplaceCuboidBlocks::end),
		MCCodecs.BLOCK_STATE.fieldOf("state").forGetter(ReplaceCuboidBlocks::state)
	).apply(instance, ReplaceCuboidBlocks::new)), CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, ReplaceCuboidBlocks::start,
		BlockPos.STREAM_CODEC, ReplaceCuboidBlocks::end,
		MCStreamCodecs.BLOCK_STATE, ReplaceCuboidBlocks::state,
		ReplaceCuboidBlocks::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public void collectSections(Level level, Set<SectionPos> sections) {
		int sminX = SectionPos.blockToSectionCoord(Math.min(start.getX(), end.getX()));
		int sminY = SectionPos.blockToSectionCoord(Math.min(start.getY(), end.getY()));
		int sminZ = SectionPos.blockToSectionCoord(Math.min(start.getZ(), end.getZ()));
		int smaxX = SectionPos.blockToSectionCoord(Math.max(start.getX(), end.getX()));
		int smaxY = SectionPos.blockToSectionCoord(Math.max(start.getY(), end.getY()));
		int smaxZ = SectionPos.blockToSectionCoord(Math.max(start.getZ(), end.getZ()));

		for (int x = sminX; x <= smaxX; x++) {
			for (int y = sminY; y <= smaxY; y++) {
				for (int z = sminZ; z <= smaxZ; z++) {
					sections.add(SectionPos.of(x, y, z));
				}
			}
		}
	}

	@Override
	public void apply(BlockModificationConsumer blocks) {
		blocks.fill(start, end, state);
	}

	@Override
	public BulkLevelModification optimize() {
		if (start.equals(end)) {
			return new PositionedBlock(start, state);
		}

		return this;
	}
}
