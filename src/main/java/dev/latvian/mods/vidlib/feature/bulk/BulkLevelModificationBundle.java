package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record BulkLevelModificationBundle(List<BulkLevelModification> list) implements BulkLevelModification, BlockModificationConsumer {
	public static final SimpleRegistryType<BulkLevelModificationBundle> TYPE = SimpleRegistryType.dynamic("bundle", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BulkLevelModification.REGISTRY.codec().listOf().fieldOf("list").forGetter(BulkLevelModificationBundle::list)
	).apply(instance, BulkLevelModificationBundle::new)), KLibStreamCodecs.listOf(BulkLevelModification.REGISTRY.streamCodec()).map(BulkLevelModificationBundle::new, BulkLevelModificationBundle::list));

	@Override
	public SimpleRegistryType<?> type() {
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
