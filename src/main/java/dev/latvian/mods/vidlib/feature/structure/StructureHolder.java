package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.bulk.OptimizedModificationBuilder;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
			if (!info.state().is(Blocks.STRUCTURE_VOID)) {
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
				if (!info.state().is(Blocks.STRUCTURE_VOID)) {
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

	public static StructureHolder fromVStruct(ByteBuf buf) {
		VarInt.read(buf); // Binary Indicator
		VarInt.read(buf); // Format Version
		VarInt.read(buf); // Data Version
		int blockCount = VarInt.read(buf);

		if (blockCount <= 0) {
			return EMPTY;
		}

		int sizeX = VarInt.read(buf);
		int sizeY = VarInt.read(buf);
		int sizeZ = VarInt.read(buf);

		int paletteCount = VarInt.read(buf);
		var palette = new Int2ReferenceArrayMap<BlockState>(paletteCount);

		for (int i = 0; i < paletteCount; i++) {
			var key = VarInt.read(buf);
			var id = ResourceLocation.parse(Utf8String.read(buf, Short.MAX_VALUE));
			var state = BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
			var diffCount = VarInt.read(buf);

			for (int d = 0; d < diffCount; d++) {
				var propertyName = Utf8String.read(buf, Short.MAX_VALUE);
				var valueName = Utf8String.read(buf, Short.MAX_VALUE);
				var property = state.getBlock().getStateDefinition().getProperty(propertyName);

				if (property != null) {
					var value = property.getValue(valueName);

					if (value.isPresent()) {
						state = state.setValue(property, Cast.to(value.get()));
					} else {
						VidLib.LOGGER.warn("Failed to parse property %s with value %s for block %s".formatted(propertyName, valueName, id));
					}
				}
			}

			palette.put(key, state);
		}

		var blocks = new Long2ObjectOpenHashMap<BlockState>(blockCount);

		for (int i = 0; i < blockCount; i++) {
			int x = VarInt.read(buf);
			int y = VarInt.read(buf);
			int z = VarInt.read(buf);
			var state = palette.get(VarInt.read(buf));

			if (state != null) {
				blocks.put(BlockPos.asLong(x, y, z), state);
			}
		}

		return new StructureHolder(blocks, new Vec3i(sizeX, sizeY, sizeZ));
	}

	public static StructureHolder fromVStruct(Path path) throws IOException {
		try (var in = new GZIPInputStream(new FastBufferedInputStream(Files.newInputStream(path)))) {
			var buf = Unpooled.wrappedBuffer(in.readAllBytes());
			return fromVStruct(buf);
		}
	}

	public static StructureHolder capture(Level level, BlockPos from, BlockPos to, @Nullable BlockFilter filter, boolean forRendering) {
		if (filter == BlockFilter.ANY.instance()) {
			filter = null;
		}

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

			if (forRendering ? state.isVisible() : !state.is(Blocks.STRUCTURE_VOID)) {
				if (filter == null || filter.test(level, pos, state)) {
					blocks.put(BlockPos.asLong(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ), state);
				}
			}
		}

		return blocks.isEmpty() ? EMPTY : new StructureHolder(blocks, size);
	}

	public boolean empty() {
		return this == EMPTY || blocks.isEmpty() || size.getX() < 1 || size.getY() < 1 || size.getZ() < 1;
	}

	public boolean hasInvisibleBlocks() {
		if (empty()) {
			return false;
		}

		for (var entry : blocks.long2ObjectEntrySet()) {
			var state = entry.getValue();

			if (!state.isVisible()) {
				return true;
			}
		}

		return false;
	}

	public StructureHolder withoutInvisibleBlocks() {
		if (empty() || !hasInvisibleBlocks()) {
			return this;
		}

		var newBlocks = new Long2ObjectOpenHashMap<BlockState>(blocks.size());

		for (var entry : blocks.long2ObjectEntrySet()) {
			var state = entry.getValue();

			if (state.isVisible()) {
				newBlocks.put(entry.getLongKey(), state);
			}
		}

		return new StructureHolder(newBlocks, size);
	}

	public StructureHolder offset(Vec3i offset) {
		return offset(offset.getX(), offset.getY(), offset.getZ());
	}

	public StructureHolder offset(int x, int y, int z) {
		if (x == 0 && y == 0 && z == 0 || empty()) {
			return this;
		}

		var offsetBlocks = new Long2ObjectOpenHashMap<BlockState>(blocks.size());

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			var state = entry.getValue();

			offsetBlocks.put(BlockPos.asLong(
				BlockPos.getX(pos) + x,
				BlockPos.getY(pos) + y,
				BlockPos.getZ(pos) + z
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

	private boolean isAir(int x, int y, int z) {
		return !blocks.containsKey(BlockPos.asLong(x, y, z));
	}

	private boolean isTransparent(int x, int y, int z) {
		var s = blocks.get(BlockPos.asLong(x, y, z));
		return s == null || !s.isVisible();
	}

	public StructureHolder shell() {
		if (empty()) {
			return this;
		}

		var newBlocks = new Long2ObjectOpenHashMap<BlockState>();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			var x = BlockPos.getX(pos);
			var y = BlockPos.getY(pos);
			var z = BlockPos.getZ(pos);
			var state = entry.getValue();

			if (isAir(x - 1, y, z) || isAir(x + 1, y, z) || isAir(x, y - 1, z) || isAir(x, y + 1, z) || isAir(x, y, z - 1) || isAir(x, y, z + 1)) {
				newBlocks.put(pos, state);
			}
		}

		return newBlocks.isEmpty() ? EMPTY : new StructureHolder(newBlocks, size);
	}

	public StructureHolder visualShell() {
		if (empty()) {
			return this;
		}

		var newBlocks = new Long2ObjectOpenHashMap<BlockState>();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			var x = BlockPos.getX(pos);
			var y = BlockPos.getY(pos);
			var z = BlockPos.getZ(pos);
			var state = entry.getValue();

			if (isTransparent(x - 1, y, z) || isTransparent(x + 1, y, z) || isTransparent(x, y - 1, z) || isTransparent(x, y + 1, z) || isTransparent(x, y, z - 1) || isTransparent(x, y, z + 1)) {
				newBlocks.put(pos, state);
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
			var x = BlockPos.getX(pos);
			var y = BlockPos.getY(pos);
			var z = BlockPos.getZ(pos);

			if (x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1) {
				newBlocks.put(entry.getLongKey(), entry.getValue());
			}
		}

		return newBlocks.isEmpty() ? EMPTY : new StructureHolder(newBlocks, new Vec3i(x1 - x0 + 1, y1 - y0 + 1, z1 - z0 + 1));
	}

	public StructureHolder slice(Vec3i min, Vec3i max) {
		return slice(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
	}

	public CompoundTag toStructureNBT() {
		var nbt = new CompoundTag();
		NbtUtils.addCurrentDataVersion(nbt);

		var sizeTag = new ListTag();
		sizeTag.add(IntTag.valueOf(size.getX()));
		sizeTag.add(IntTag.valueOf(size.getY()));
		sizeTag.add(IntTag.valueOf(size.getZ()));
		nbt.put("size", sizeTag);

		var palette = new Reference2IntOpenHashMap<BlockState>();
		palette.defaultReturnValue(-1);
		var paletteTag = new ListTag();
		var blocksTag = new ListTag();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			var state = entry.getValue();

			int stateId = palette.getInt(state);

			if (stateId == -1) {
				stateId = palette.size();
				palette.put(state, stateId);
				paletteTag.add(NbtUtils.writeBlockState(state));
			}

			var blockTag = new CompoundTag();
			var posTag = new ListTag();
			posTag.add(IntTag.valueOf(BlockPos.getX(pos)));
			posTag.add(IntTag.valueOf(BlockPos.getY(pos)));
			posTag.add(IntTag.valueOf(BlockPos.getZ(pos)));
			blockTag.put("pos", posTag);
			blockTag.put("state", IntTag.valueOf(stateId));
			blocksTag.add(blockTag);
		}

		nbt.put("palette", paletteTag);
		nbt.put("blocks", blocksTag);
		return nbt;
	}

	public void toVStruct(ByteBuf buf) {
		VarInt.write(buf, 0); // Binary Indicator
		VarInt.write(buf, 0); // Format Version
		VarInt.write(buf, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
		VarInt.write(buf, blocks.size());

		if (blocks.isEmpty()) {
			return;
		}

		VarInt.write(buf, size.getX());
		VarInt.write(buf, size.getY());
		VarInt.write(buf, size.getZ());

		var palette = new Reference2IntOpenHashMap<BlockState>();
		palette.defaultReturnValue(-1);

		for (var entry : blocks.long2ObjectEntrySet()) {
			var state = entry.getValue();

			if (palette.getInt(state) == -1) {
				palette.put(state, palette.size());
			}
		}

		VarInt.write(buf, palette.size());

		for (var entry : palette.reference2IntEntrySet()) {
			VarInt.write(buf, entry.getIntValue());
			var state = entry.getKey();
			var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
			Utf8String.write(buf, id.getNamespace().equals("minecraft") ? id.getPath() : id.toString(), Short.MAX_VALUE);
			var defaultState = state.getBlock().defaultBlockState();

			if (state == defaultState) {
				VarInt.write(buf, 0);
			} else {
				var diff = new Reference2ObjectOpenHashMap<Property<?>, String>();

				for (var property : state.getProperties()) {
					var defaultValue = defaultState.getValue(property);
					var value = state.getValue(property);

					if (!Objects.equals(defaultValue, value)) {
						diff.put(property, property.getName(Cast.to(value)));
					}
				}

				VarInt.write(buf, diff.size());

				for (var entry2 : diff.reference2ObjectEntrySet()) {
					var property = entry2.getKey();
					var value = entry2.getValue();
					Utf8String.write(buf, property.getName(), Short.MAX_VALUE);
					Utf8String.write(buf, value, Short.MAX_VALUE);
				}
			}
		}

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = entry.getLongKey();
			VarInt.write(buf, BlockPos.getX(pos));
			VarInt.write(buf, BlockPos.getY(pos));
			VarInt.write(buf, BlockPos.getZ(pos));
			VarInt.write(buf, palette.getInt(entry.getValue()));
		}
	}

	public void toVStruct(Path path) throws IOException {
		var buf = Unpooled.buffer();
		toVStruct(buf);

		try (var out = new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(path)))) {
			buf.readBytes(out, buf.readableBytes());
		}
	}
}
