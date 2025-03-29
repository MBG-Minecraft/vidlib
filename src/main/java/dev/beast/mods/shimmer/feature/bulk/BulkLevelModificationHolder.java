package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.net.S2CPacketBundleBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.List;

public class BulkLevelModificationHolder implements BlockModificationConsumer {
	public static boolean debug = false;

	public record UndoableBulkModification(Reference2ObjectOpenHashMap<BlockState, LongArrayList> undo) implements UndoableModification {
		@Override
		public void undo(Level level, BlockModificationConsumer consumer) {
			for (var entry : undo.entrySet()) {
				var state = entry.getKey();
				var positions = entry.getValue();

				for (long pos : positions) {
					consumer.set(BlockPos.of(pos), state);
				}
			}
		}
	}

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

	public int apply(Level level, BulkLevelModification modification, boolean undoable) {
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

		var rerender = new LongOpenHashSet();
		var undo = new Reference2ObjectOpenHashMap<BlockState, LongArrayList>();
		var server = !level.isClientSide();
		var packets = server ? new S2CPacketBundleBuilder(level) : null;

		for (var sd : sections.values()) {
			if (!sd.modified) {
				if (debug) {
					Shimmer.LOGGER.info("Skipped unmodified bulk edit section @ %d, %d, %d".formatted(sd.pos.x(), sd.pos.y(), sd.pos.z()));
				}

				continue;
			}

			if (sd.levelChunk == null || sd.levelChunkSection == null) {
				if (debug) {
					Shimmer.LOGGER.info("Skipped invalid bulk edit section @ %d, %d, %d".formatted(sd.pos.x(), sd.pos.y(), sd.pos.z()));
				}

				continue;
			}

			if (sd.hasOnlyAir && sd.levelChunkSection.hasOnlyAir()) {
				if (debug) {
					Shimmer.LOGGER.info("Skipped empty bulk edit section @ %d, %d, %d".formatted(sd.pos.x(), sd.pos.y(), sd.pos.z()));
				}

				continue;
			}

			boolean rwest = false;
			boolean reast = false;
			boolean rdown = false;
			boolean rup = false;
			boolean rnorth = false;
			boolean rsouth = false;
			var toClient = server ? new OptimizedModificationBuilder() : null;

			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						var state = sd.getBlock(x, y, z);

						if (state != null) {
							blockPos.setX(sd.pos.minBlockX() + x);
							blockPos.setY(sd.pos.minBlockY() + y);
							blockPos.setZ(sd.pos.minBlockZ() + z);

							try {
								var prevState = sd.levelChunk.getBlockState(blockPos);

								if (prevState != state) {
									sd.levelChunk.setBlockState(blockPos, state, false);

									if (server) {
										toClient.set(blockPos, state);
									}

									count++;

									if (undoable && server) {
										undo.computeIfAbsent(prevState, o -> new LongArrayList()).add(blockPos.asLong());
									}

									if (x == 0) {
										rwest = true;
									} else if (x == 15) {
										reast = true;
									}

									if (y == 0) {
										rdown = true;
									} else if (y == 15) {
										rup = true;
									}

									if (z == 0) {
										rnorth = true;
									} else if (z == 15) {
										rsouth = true;
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			}

			if (server) {
				var mod = toClient.build();

				if (mod != BulkLevelModification.NONE) {
					packets.s2c(new SectionModifiedPayload(undoable, sd.pos, mod));
				}
			}

			sd.levelChunk.markUnsaved();

			rerender.add(sd.pos.asLong());

			if (server) {
				if (rwest) {
					rerender.add(SectionPos.of(sd.pos.x() - 1, sd.pos.y(), sd.pos.z()).asLong());
				}

				if (reast) {
					rerender.add(SectionPos.of(sd.pos.x() + 1, sd.pos.y(), sd.pos.z()).asLong());
				}

				if (rdown) {
					rerender.add(SectionPos.of(sd.pos.x(), sd.pos.y() - 1, sd.pos.z()).asLong());
				}

				if (rup) {
					rerender.add(SectionPos.of(sd.pos.x(), sd.pos.y() + 1, sd.pos.z()).asLong());
				}

				if (rnorth) {
					rerender.add(SectionPos.of(sd.pos.x(), sd.pos.y(), sd.pos.z() - 1).asLong());
				}

				if (rsouth) {
					rerender.add(SectionPos.of(sd.pos.x(), sd.pos.y(), sd.pos.z() + 1).asLong());
				}
			}
		}

		if (server) {
			packets.s2c(new RedrawChunkSectionsPayload(new LongArrayList(rerender), false));

			if (undoable) {
				level.addUndoable(new UndoableBulkModification(undo));
			}

			packets.send(level);
		}

		if (debug) {
			Shimmer.LOGGER.info("Modified " + count + " blocks");
		}

		return count;
	}
}
