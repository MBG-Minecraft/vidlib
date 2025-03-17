package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.Shimmer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.List;

public class BulkLevelModificationHolder implements BlockModificationConsumer {
	public static boolean debug = false;

	private final Long2ObjectMap<SectionData> sections = new Long2ObjectOpenHashMap<>();

	public SectionData getSection(SectionPos sectionPos) {
		return sections.get(sectionPos.asLong());
	}

	@Override
	public void set(BlockPos pos, BlockState state) {
		getSection(SectionPos.of(pos)).setBlock(pos.getX(), pos.getY(), pos.getZ(), state);
	}

	@Override
	public void fillSection(SectionPos pos, BlockState state) {
		getSection(pos).fill(state);
	}

	@Override
	public void applyPalettes(SectionPos pos, List<BlockPalette> palettes) {
		var section = getSection(pos);

		for (var palette : palettes) {
			for (int i = 0; i < palette.positions().size(); i++) {
				section.setBlock(palette.positions().getShort(i) & 0xFFFF, palette.state());
			}
		}
	}

	public int apply(Level level, BulkLevelModification modification) {
		var sectionSet = new HashSet<SectionPos>();
		modification.collectSections(level, sectionSet);

		for (var pos : sectionSet) {
			var sectionData = new SectionData(pos);

			if (!level.isOutsideBuildHeight(pos.center()) && level.getChunk(pos.getX(), pos.getZ()) instanceof LevelChunk levelChunk) {
				sectionData.levelChunk = levelChunk;
				sectionData.levelChunkSection = levelChunk.getSection(level.getSectionIndexFromSectionY(pos.y()));
			}

			sectionData.modified = false;
			sections.put(pos.asLong(), sectionData);
		}

		modification.apply(this);

		var blockPos = new BlockPos.MutableBlockPos();
		int count = 0;

		for (var sectionData : sections.values()) {
			if (!sectionData.modified) {
				if (debug) {
					Shimmer.LOGGER.info("Skipped unmodified bulk edit section @ %d, %d, %d".formatted(sectionData.pos.x(), sectionData.pos.y(), sectionData.pos.z()));
				}

				continue;
			}

			if (sectionData.levelChunk == null || sectionData.levelChunkSection == null) {
				if (debug) {
					Shimmer.LOGGER.info("Skipped invalid bulk edit section @ %d, %d, %d".formatted(sectionData.pos.x(), sectionData.pos.y(), sectionData.pos.z()));
				}

				continue;
			}

			if (sectionData.hasOnlyAir && sectionData.levelChunkSection.hasOnlyAir()) {
				if (debug) {
					Shimmer.LOGGER.info("Skipped empty bulk edit section @ %d, %d, %d".formatted(sectionData.pos.x(), sectionData.pos.y(), sectionData.pos.z()));
				}

				continue;
			}

			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						var state = sectionData.getBlock(x, y, z);

						if (state != null) {
							blockPos.setX(sectionData.pos.minBlockX() + x);
							blockPos.setY(sectionData.pos.minBlockY() + y);
							blockPos.setZ(sectionData.pos.minBlockZ() + z);

							try {
								if (sectionData.levelChunk.getBlockState(blockPos) != state) {
									sectionData.levelChunk.setBlockState(blockPos, state, false);
									count++;
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			}

			sectionData.levelChunk.markUnsaved();
			level.redrawSection(sectionData.pos.x(), sectionData.pos.y(), sectionData.pos.z(), false);
		}

		if (debug) {
			Shimmer.LOGGER.info("Modified " + count + " blocks");
		}

		return count;
	}
}
