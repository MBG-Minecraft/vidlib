package dev.beast.mods.shimmer.feature.bulk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

public class OptimizedModificationBuilder implements BlockModificationConsumer {
	public final Long2ObjectMap<Short2ObjectMap<BlockState>> sections = new Long2ObjectOpenHashMap<>();

	private Short2ObjectMap<BlockState> getSection(SectionPos pos) {
		return sections.computeIfAbsent(pos.asLong(), k -> new Short2ObjectOpenHashMap<>());
	}

	@Override
	public void set(BlockPos pos, BlockState state) {
		var map = getSection(SectionPos.of(pos));
		map.put((short) SectionData.index(pos), state);
	}

	@Override
	public void fillSection(SectionPos pos, BlockState state) {
		var map = getSection(pos);

		for (int i = 0; i < 4096; i++) {
			map.put((short) i, state);
		}
	}

	@Override
	public void applyPalettes(SectionPos pos, List<BlockPalette> palettes) {
		var map = getSection(pos);

		for (var palette : palettes) {
			for (int i = 0; i < palette.positions().size(); i++) {
				map.put(palette.positions().getShort(i), palette.state());
			}
		}
	}

	private static BulkLevelModification of(SectionPos pos, Short2ObjectMap<BlockState> map) {
		if (map.isEmpty()) {
			return BulkLevelModification.NONE;
		}

		var reverseMap = new IdentityHashMap<BlockState, BlockPalette>();

		for (var entry : map.short2ObjectEntrySet()) {
			reverseMap.computeIfAbsent(entry.getValue(), state -> new BlockPalette(state, new ShortArrayList())).positions().add(entry.getShortKey());
		}

		var list = List.copyOf(reverseMap.values());

		if (list.size() == 1 && list.getFirst().positions().size() == 4096) {
			return new ReplaceAllSectionBlocks(pos, list.getFirst().state());
		}

		return new ReplaceSectionBlocks(pos, list);
	}

	public BulkLevelModification build() {
		if (sections.isEmpty()) {
			return BulkLevelModification.NONE;
		} else if (sections.size() == 1) {
			var entry = sections.long2ObjectEntrySet().iterator().next();
			return of(SectionPos.of(entry.getLongKey()), entry.getValue());
		} else {
			var list = new ArrayList<BulkLevelModification>();

			for (var entry : sections.long2ObjectEntrySet()) {
				var s = of(SectionPos.of(entry.getLongKey()), entry.getValue());

				if (s != BulkLevelModification.NONE) {
					list.add(s);
				}
			}

			return BulkLevelModification.allOf(list);
		}
	}
}
