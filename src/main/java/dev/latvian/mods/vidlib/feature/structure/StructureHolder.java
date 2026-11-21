package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.block.KLibBlockStateProperties;
import dev.latvian.mods.klib.block.filter.SimpleBlockPredicate;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record StructureHolder(Long2ObjectMap<BlockState> blocks, Vec3i size) {
	public static final StructureHolder EMPTY = new StructureHolder(Long2ObjectMaps.emptyMap(), Vec3i.ZERO);

	public static List<StructureHolder> allOf(StructureTemplate template) {
		var templateSize = template.getSize();

		if (templateSize.getX() <= 0 || templateSize.getY() <= 0 || templateSize.getZ() <= 0 || template.palettes.isEmpty()) {
			return List.of();
		}

		var holders = new ArrayList<StructureHolder>(template.palettes.size());

		for (var palette : template.palettes) {
			var blocks = new Long2ObjectOpenHashMap<BlockState>();

			for (var info : palette.blocks()) {
				if (!info.state().is(Blocks.STRUCTURE_VOID)) {
					blocks.put(info.pos().asLong(), info.state());
				}
			}

			if (!blocks.isEmpty()) {
				holders.add(new StructureHolder(blocks, templateSize));
			}
		}

		return holders;
	}

	public static Supplier<StructureHolder> refSupplier(Identifier structureId) {
		return () -> {
			var lazy = StructureStorage.CLIENT.get(structureId.toString());

			if (lazy == null) {
				lazy = StructureStorage.findLocal(structureId);
			}

			if (lazy == null) {
				return null;
			}

			var list = lazy.get();
			return list.isEmpty() ? null : list.getFirst();
		};
	}

	public static StructureHolder capture(Level level, BlockPos from, BlockPos to, @Nullable SimpleBlockPredicate filter, boolean onlyExposed, boolean forRendering) {
		int minX = Math.min(from.getX(), to.getX());
		int minY = Math.min(from.getY(), to.getY());
		int minZ = Math.min(from.getZ(), to.getZ());
		int maxX = Math.max(from.getX(), to.getX());
		int maxY = Math.max(from.getY(), to.getY());
		int maxZ = Math.max(from.getZ(), to.getZ());
		var size = new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);

		var blocks = new Long2ObjectOpenHashMap<BlockState>();
		var partialCache = new Long2IntOpenHashMap();
		partialCache.defaultReturnValue(-1);
		var mutablePos = new BlockPos.MutableBlockPos();

		for (var pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
			var state = level.getBlockState(pos);

			if (forRendering ? KLibBlockStateProperties.of(state).visible() : !state.is(Blocks.STRUCTURE_VOID)) {
				if (filter == null || filter.test(level, pos, state)) {
					if (onlyExposed && !BlockUtils.isBlockExposed(level, partialCache, pos.getX(), pos.getY(), pos.getZ(), mutablePos)) {
						continue;
					}

					blocks.put(BlockPos.asLong(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ), state);
				}
			}
		}

		return blocks.isEmpty() ? EMPTY : new StructureHolder(blocks, size);
	}

	public boolean empty() {
		return this == EMPTY || blocks.isEmpty() || size.getX() < 1 || size.getY() < 1 || size.getZ() < 1;
	}

	public StructureHolder offset(int x, int y, int z) {
		if (x == 0 && y == 0 && z == 0 || empty()) {
			return this;
		}

		var offsetBlocks = new Long2ObjectOpenHashMap<BlockState>(blocks.size());

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			offsetBlocks.put(BlockPos.asLong(
				BlockPos.getX(pos) + x,
				BlockPos.getY(pos) + y,
				BlockPos.getZ(pos) + z
			), entry.getValue());
		}

		return new StructureHolder(offsetBlocks, size);
	}

	private boolean isPartial(int x, int y, int z) {
		var state = blocks.get(BlockPos.asLong(x, y, z));
		return state == null || KLibBlockStateProperties.of(state).partial();
	}

	public StructureHolder visualShell() {
		if (empty()) {
			return this;
		}

		var newBlocks = new Long2ObjectOpenHashMap<BlockState>();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			int x = BlockPos.getX(pos);
			int y = BlockPos.getY(pos);
			int z = BlockPos.getZ(pos);

			if (isPartial(x - 1, y, z) || isPartial(x + 1, y, z) || isPartial(x, y - 1, z) || isPartial(x, y + 1, z) || isPartial(x, y, z - 1) || isPartial(x, y, z + 1)) {
				newBlocks.put(pos, entry.getValue());
			}
		}

		return newBlocks.isEmpty() ? EMPTY : new StructureHolder(newBlocks, size);
	}

	public StructureHolder slice(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if (empty()) {
			return this;
		}

		int x0 = Math.min(minX, maxX);
		int y0 = Math.min(minY, maxY);
		int z0 = Math.min(minZ, maxZ);
		int x1 = Math.max(minX, maxX);
		int y1 = Math.max(minY, maxY);
		int z1 = Math.max(minZ, maxZ);
		var newBlocks = new Long2ObjectOpenHashMap<BlockState>();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			int x = BlockPos.getX(pos);
			int y = BlockPos.getY(pos);
			int z = BlockPos.getZ(pos);

			if (x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1) {
				newBlocks.put(pos, entry.getValue());
			}
		}

		return newBlocks.isEmpty() ? EMPTY : new StructureHolder(newBlocks, new Vec3i(x1 - x0 + 1, y1 - y0 + 1, z1 - z0 + 1));
	}

	public void toVStruct(Path path) throws IOException {
		StructureIO.writeVStruct(this, path);
	}

	public void toSchem(Path path) throws IOException {
		StructureIO.writeSchem(this, path);
	}
}