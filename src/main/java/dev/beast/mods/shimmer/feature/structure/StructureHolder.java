package dev.beast.mods.shimmer.feature.structure;

import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.util.registry.RegistryRef;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record StructureHolder(Long2ObjectMap<BlockState> blocks, Vec3i size) {
	public static final StructureHolder EMPTY = new StructureHolder(Long2ObjectMaps.emptyMap(), Vec3i.ZERO);

	public static final StreamCodec<ByteBuf, StructureHolder> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public StructureHolder decode(ByteBuf buf) {
			int sx = VarInt.read(buf);
			int sy = VarInt.read(buf);
			int sz = VarInt.read(buf);
			int s = VarInt.read(buf);

			var blocks = new Long2ObjectOpenHashMap<BlockState>(s);

			for (int i = 0; i < s; i++) {
				var pos = buf.readLong();
				var state = Block.stateById(VarInt.read(buf));
				blocks.put(pos, state);
			}

			return new StructureHolder(blocks, new Vec3i(sx, sy, sz));
		}

		@Override
		public void encode(ByteBuf buf, StructureHolder value) {
			VarInt.write(buf, value.size().getX());
			VarInt.write(buf, value.size().getY());
			VarInt.write(buf, value.size().getZ());
			VarInt.write(buf, value.blocks.size());

			for (var entry : value.blocks.long2ObjectEntrySet()) {
				buf.writeLong(entry.getLongKey());
				VarInt.write(buf, Block.getId(entry.getValue()));
			}
		}
	};

	public static StructureHolder of(StructureTemplate template, @Nullable RandomSource random) {
		if (template.getSize().getX() == 0 || template.getSize().getY() == 0 || template.getSize().getZ() == 0) {
			return EMPTY;
		} else if (template.palettes.isEmpty()) {
			return EMPTY;
		}

		var blocks = new Long2ObjectOpenHashMap<BlockState>();
		var palette = random == null ? template.palettes.getFirst() : template.palettes.get(random.nextInt(template.palettes.size()));

		for (var info : palette.blocks()) {
			if (!info.state().isAir() && info.state().getRenderShape() != RenderShape.INVISIBLE) {
				blocks.put(info.pos().asLong(), info.state());
			}
		}

		if (blocks.isEmpty()) {
			return EMPTY;
		}

		return new StructureHolder(blocks, template.getSize());
	}

	public static List<StructureHolder> allOf(StructureTemplate template) {
		if (template.getSize().getX() == 0 || template.getSize().getY() == 0 || template.getSize().getZ() == 0) {
			return List.of();
		} else if (template.palettes.isEmpty()) {
			return List.of();
		}

		var holders = new ArrayList<StructureHolder>(template.palettes.size());

		for (var palette : template.palettes) {
			var blocks = new Long2ObjectOpenHashMap<BlockState>();

			for (var info : palette.blocks()) {
				if (!info.state().isAir() && info.state().getRenderShape() != RenderShape.INVISIBLE) {
					blocks.put(info.pos().asLong(), info.state());
				}
			}

			if (!blocks.isEmpty()) {
				holders.add(new StructureHolder(blocks, template.getSize()));
			}
		}

		return holders;
	}

	public static Supplier<StructureHolder> ref(StructureHolder holder) {
		return () -> holder;
	}

	public static Supplier<StructureHolder> refSupplier(@Nullable RegistryRef<LazyStructures> structureRef) {
		return () -> structureRef == null ? null : structureRef.get().get().getFirst();
	}

	public static StructureHolder capture(BlockGetter level, BlockPos from, BlockPos to) {
		var blocks = new Long2ObjectOpenHashMap<BlockState>();
		var minX = Math.min(from.getX(), to.getX());
		var minY = Math.min(from.getY(), to.getY());
		var minZ = Math.min(from.getZ(), to.getZ());
		var maxX = Math.max(from.getX(), to.getX());
		var maxY = Math.max(from.getY(), to.getY());
		var maxZ = Math.max(from.getZ(), to.getZ());
		var size = new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);

		for (var pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
			var state = level.getBlockState(pos);

			if (!state.isAir()) {
				blocks.put(BlockPos.asLong(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ), state);
			}
		}

		return new StructureHolder(blocks, size);
	}

	public static StructureHolder capture(Level level, BlockPos from, BlockPos to, BlockFilter filter) {
		var blocks = new Long2ObjectOpenHashMap<BlockState>();
		var minX = Math.min(from.getX(), to.getX());
		var minY = Math.min(from.getY(), to.getY());
		var minZ = Math.min(from.getZ(), to.getZ());
		var maxX = Math.max(from.getX(), to.getX());
		var maxY = Math.max(from.getY(), to.getY());
		var maxZ = Math.max(from.getZ(), to.getZ());
		var size = new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);

		for (var pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
			var state = level.getBlockState(pos);

			if (!state.isAir() && filter.test(level, pos, state)) {
				blocks.put(BlockPos.asLong(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ), state);
			}
		}

		return new StructureHolder(blocks, size);
	}

	public boolean empty() {
		return this == EMPTY || blocks.isEmpty() || size.getX() < 1 || size.getY() < 1 || size.getZ() < 1;
	}

	public StructureHolder offset(BlockPos offset) {
		if (offset.equals(BlockPos.ZERO)) {
			return this;
		}

		var offsetBlocks = new Long2ObjectOpenHashMap<BlockState>(blocks.size());

		for (var entry : offsetBlocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			var state = entry.getValue();

			offsetBlocks.put(BlockPos.asLong(
				BlockPos.getX(pos) + offset.getX(),
				BlockPos.getY(pos) + offset.getY(),
				BlockPos.getZ(pos) + offset.getZ()
			), state);
		}

		return new StructureHolder(offsetBlocks, size);
	}

	public BulkLevelModification createModification() {
		if (empty()) {
			return BulkLevelModification.NONE;
		}

		var builder = new OptimizedModificationBuilder();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = BlockPos.of(entry.getLongKey());
			var state = entry.getValue();
			builder.set(pos, state);
		}

		return builder.build();
	}

	public BulkLevelModification createModification(
		BlockPos pos,
		BlockPos offset,
		Mirror mirror,
		Rotation rotation,
		BlockPos rotationPivot
	) {
		if (empty()) {
			return BulkLevelModification.NONE;
		}

		var builder = new OptimizedModificationBuilder();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var blockPos = StructureTemplate.transform(BlockPos.of(entry.getLongKey()).offset(offset), mirror, rotation, rotationPivot).offset(pos);
			var state = entry.getValue().mirror(mirror).rotate(rotation);
			builder.set(blockPos, state);
		}

		return builder.build();
	}
}
