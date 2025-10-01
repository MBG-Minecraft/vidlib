package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.klib.util.MessageConsumer;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class CurrentStructureCapture {
	public final Long2ObjectMap<BlockState> blocks = new Long2ObjectOpenHashMap<>();
	public final Long2ObjectMap<BlockState> water = new Long2ObjectOpenHashMap<>();
	public int minX = Integer.MAX_VALUE;
	public int minY = Integer.MAX_VALUE;
	public int minZ = Integer.MAX_VALUE;
	public int maxX = Integer.MIN_VALUE;
	public int maxY = Integer.MIN_VALUE;
	public int maxZ = Integer.MIN_VALUE;
	public StructureHolder blockStructure;
	public StructureHolder waterStructure;

	public void build(MessageConsumer source) {
		minX = Integer.MAX_VALUE;
		minY = Integer.MAX_VALUE;
		minZ = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		maxY = Integer.MIN_VALUE;
		maxZ = Integer.MIN_VALUE;

		for (var c : blocks.long2ObjectEntrySet()) {
			var pos = BlockPos.of(c.getLongKey());
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}

		for (var c : water.long2ObjectEntrySet()) {
			var pos = BlockPos.of(c.getLongKey());
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}

		var size = new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
		source.tell("Building a %d x %d x %d (%,d blocks) structure...".formatted(size.getX(), size.getY(), size.getZ(), size.getX() * size.getY() * size.getZ()));

		blockStructure = new StructureHolder(blocks, size).offset(-minX, -minY, -minZ);
		waterStructure = new StructureHolder(water, size).offset(-minX, -minY, -minZ);
	}

	public void move(Vec3i pos) {
		int dx = pos.getX() - minX;
		int dy = pos.getY() - minY;
		int dz = pos.getZ() - minZ;

		minX += dx;
		minY += dy;
		minZ += dz;
		maxX += dx;
		maxY += dy;
		maxZ += dz;
	}

	public void addBlocks(Level level, MessageConsumer source, BlockPos start, BlockPos end) {
		var partialCache = new Long2IntOpenHashMap();
		partialCache.defaultReturnValue(-1);

		var minX = Math.min(start.getX(), end.getX());
		var minY = Math.min(start.getY(), end.getY());
		var minZ = Math.min(start.getZ(), end.getZ());
		var maxX = Math.max(start.getX(), end.getX());
		var maxY = Math.max(start.getY(), end.getY());
		var maxZ = Math.max(start.getZ(), end.getZ());
		var volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
		source.tell("Scanning %,d block area...".formatted(volume));
		var capture = StructureHolder.capture(level, start, end, StructureCapture.buildFilter(), true, true).offset(minX, minY, minZ);

		for (var entry : capture.blocks().long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			var state = entry.getValue();

			if (!state.isAir()) {
				var fluid = state.getFluidState();

				if (state.liquid() && fluid.getType().isSame(Fluids.WATER)) {
					water.put(pos, state);
				} else if (fluid.isEmpty()) {
					blocks.put(pos, state);
				} else if (entry.getValue() instanceof SimpleWaterloggedBlock) {
					try {
						var blockState = state.setValue(BlockStateProperties.WATERLOGGED, false);
						var fluidState = state.getFluidState().createLegacyBlock();
						blocks.put(pos, blockState);
						water.put(pos, fluidState);
					} catch (Exception ex) {
						blocks.put(pos, state);
					}
				} else {
					blocks.put(pos, state);
				}
			}
		}


		source.tell("Added %,d blocks".formatted(capture.blocks().size()));
	}
}
