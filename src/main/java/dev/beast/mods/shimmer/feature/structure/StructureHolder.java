package dev.beast.mods.shimmer.feature.structure;

import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.util.Lazy;
import dev.beast.mods.shimmer.util.registry.RegistryRef;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record StructureHolder(Long2ObjectMap<BlockState> blocks, Vec3i size) {
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

	public static StructureHolder of(StructureTemplate template) {
		var blocks = new Long2ObjectOpenHashMap<BlockState>();

		for (var palette : template.palettes) {
			for (var info : palette.blocks()) {
				if (!info.state().isAir() && info.state().getRenderShape() != RenderShape.INVISIBLE) {
					blocks.put(info.pos().asLong(), info.state());
				}
			}
		}

		return new StructureHolder(blocks, template.getSize());
	}

	public static Supplier<StructureHolder> refSupplier(@Nullable RegistryRef<Lazy<StructureTemplate>> structureRef) {
		return () -> structureRef == null ? null : of(structureRef.get().get());
	}

	public static StructureHolder capture(BlockGetter level, BlockPos from, BlockPos to) {
		var blocks = new Long2ObjectOpenHashMap<BlockState>();
		var size = new Vec3i(Math.abs(to.getX() - from.getX()), Math.abs(to.getY() - from.getY()), Math.abs(to.getZ() - from.getZ()));

		for (var pos : BlockPos.betweenClosed(from, to)) {
			var state = level.getBlockState(pos);

			if (!state.isAir()) {
				blocks.put(pos.asLong(), state);
			}
		}

		return new StructureHolder(blocks, size);
	}

	public static StructureHolder capture(Level level, BlockPos from, BlockPos to, BlockFilter filter) {
		var blocks = new Long2ObjectOpenHashMap<BlockState>();
		var size = new Vec3i(Math.abs(to.getX() - from.getX()), Math.abs(to.getY() - from.getY()), Math.abs(to.getZ() - from.getZ()));

		for (var pos : BlockPos.betweenClosed(from, to)) {
			var state = level.getBlockState(pos);

			if (!state.isAir() && filter.test(level, pos, state)) {
				blocks.put(pos.asLong(), state);
			}
		}

		return new StructureHolder(blocks, size);
	}

	public int place(Level level, BlockPos offset) {
		var builder = new OptimizedModificationBuilder();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = BlockPos.of(entry.getLongKey());
			var state = entry.getValue();
			builder.set(offset == BlockPos.ZERO ? pos : pos.offset(offset), state);
		}

		return level.bulkModify(builder.build());
	}
}
