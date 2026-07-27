package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.klib.block.PositionedBlock;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record BulkLevelModificationBundle(List<Ref<BulkLevelModification>> list) implements BulkLevelModification, BlockModificationConsumer {
	public static final DynamicType<ByteBuf, BulkLevelModification> TYPE = DynamicType.create(
		"bundle",
		"list",
		BulkLevelModification.CODEC.listOf(),
		KLibStreamCodecs.listOf(BulkLevelModification.STREAM_CODEC),
		BulkLevelModificationBundle::new,
		BulkLevelModificationBundle::list
	);

	@Override
	public DynamicType<ByteBuf, BulkLevelModification> type() {
		return TYPE;
	}

	@Override
	public void add(BulkLevelModification modification) {
		list.add(modification);
	}

	@Override
	public void set(BlockPos pos, BlockState state) {
		list.add(new PositionedBlock(pos, state));
	}

	@Override
	public void fill(BlockPos start, BlockPos end, BlockState state) {
		list.add(new ReplaceCuboidBlocks(start, end, state));
	}

	@Override
	public void fillSection(SectionPos pos, BlockState state) {
		list.add(new ReplaceAllSectionBlocks(pos, state));
	}

	@Override
	public void applyPalettes(SectionPos pos, List<BlockPalette> palettes) {
		list.add(new ReplaceSectionBlocks(pos, palettes));
	}

	@Override
	public void collectSections(Level level, Set<SectionPos> sections) {
		for (var modification : list) {
			modification.collectSections(level, sections);
		}
	}

	@Override
	public void apply(BlockModificationConsumer blocks) {
		for (var modification : list) {
			modification.apply(blocks);
		}
	}

	@Override
	public BulkLevelModification optimize() {
		var tempList = new ArrayList<BulkLevelModification>(list.size());

		for (var m : list) {
			var o = m.optimize();

			if (o instanceof BulkLevelModificationBundle(List<BulkLevelModification> list1)) {
				tempList.addAll(list1);
			} else {
				tempList.add(o);
			}
		}

		return BulkLevelModification.allOf(tempList);
	}
}
