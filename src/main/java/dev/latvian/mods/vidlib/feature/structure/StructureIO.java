package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.vidlib.VidLib;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Structure file IO.
 * <p>
 * Native {@code .vstruct} v2 layout (ZSTD compressed):
 * <pre>
 * magic "VLST", VarInt version (2)
 * VarInt sizeX, sizeY, sizeZ, VarInt totalBlockCount
 * VarInt globalPaletteSize
 *   per entry: block id string (namespace omitted for minecraft), VarInt propCount, prop name + value strings
 * VarInt sectionCount
 *   per section:
 *     VarInt sectionX, VarInt sectionY, VarInt sectionZ (signed section coords, not bounded by size)
 *     byte storageType:
 *       0 SINGLE: VarInt global index + 1 (section completely filled with one state)
 *       1 SPARSE: VarInt localPaletteSize, (VarInt global index + 1)*,
 *                  VarInt blockCount, (VarInt local index delta, VarInt local palette index)*
 *       2 DENSE:  VarInt localPaletteSize (includes air as 0), (VarInt global index + 1)*,
 *                 4096 local palette indices bit-packed LSB-first
 * </pre>
 * Legacy v0 (GZIP) files are still readable. WorldEdit Sponge schematic v2 and v3 are supported both ways.
 */
public final class StructureIO {
	public static final byte[] VSTRUCT_MAGIC = {'V', 'L', 'S', 'T'};
	public static final int VSTRUCT_VERSION = 2;
	private static final int SECTION_SINGLE = 0;
	private static final int SECTION_SPARSE = 1;
	private static final int SECTION_DENSE = 2;

	private StructureIO() {
	}

	public static List<StructureHolder> read(Identifier id, InputStream stream) throws IOException {
		var bytes = stream.readAllBytes();
		var path = id.getPath();

		if (path.endsWith(".schem")) {
			return List.of(readSchem(bytes));
		} else if (path.endsWith(".nbt")) {
			return readStructureNbt(bytes);
		} else if (path.endsWith(".vstruct")) {
			return List.of(readVStruct(bytes));
		}

		// Storage keys don't keep the file extension, so sniff the format from the content instead
		var data = decompress(bytes);

		if (data.length >= 4 && data[0] == 'V' && data[1] == 'L' && data[2] == 'S' && data[3] == 'T') {
			return List.of(readVStruct(bytes));
		} else if (data.length >= 2 && data[0] == 0 && data[1] == 0) {
			return List.of(readVStruct(bytes));
		} else if (data.length >= 1 && data[0] == 0x0A) {
			// NBT: Sponge schematic or its WorldEdit import format (.schem), otherwise a structure template (.nbt)
			try {
				var schematic = readSchem(bytes);

				if (!schematic.empty()) {
					return List.of(schematic);
				}
			} catch (Exception ignore) {
			}

			return readStructureNbt(bytes);
		}

		throw new IOException("Unknown structure format for " + id);
	}

	// ==== vstruct ====

	public static StructureHolder readVStruct(byte[] raw) throws IOException {
		var in = new ByteReader(decompress(raw));

		if (in.matches(VSTRUCT_MAGIC)) {
			in.skip(VSTRUCT_MAGIC.length);
			int version = in.varInt();

			if (version != VSTRUCT_VERSION) {
				throw new IOException("Unsupported vstruct version " + version);
			}

			return readVStruct2(in);
		}

		int indicator = in.varInt();
		int version = in.varInt();

		if (indicator != 0 || version != 0) {
			throw new IOException("Unknown vstruct format " + indicator + "/" + version);
		}

		return readVStruct0(in);
	}

	private static StructureHolder readVStruct0(ByteReader in) throws IOException {
		in.varInt(); // data version
		int blockCount = in.varInt();

		if (blockCount <= 0) {
			return StructureHolder.EMPTY;
		}

		int sizeX = in.varInt();
		int sizeY = in.varInt();
		int sizeZ = in.varInt();
		int paletteCount = in.varInt();
		var palette = new Int2ReferenceArrayMap<BlockState>(Math.max(1, paletteCount));

		for (int i = 0; i < paletteCount; i++) {
			int key = in.varInt();
			var state = parseBlockString(in.string());
			int diffCount = in.varInt();

			for (int d = 0; d < diffCount; d++) {
				var propertyName = in.string();
				var valueName = in.string();
				var property = state.getBlock().getStateDefinition().getProperty(propertyName);

				if (property != null) {
					state = applyProperty(state, property, valueName);
				}
			}

			palette.put(key, state);
		}

		var blocks = new Long2ObjectOpenHashMap<BlockState>(blockCount);

		for (int i = 0; i < blockCount; i++) {
			int x = in.varInt();
			int y = in.varInt();
			int z = in.varInt();
			var state = palette.get(in.varInt());

			if (state != null) {
				blocks.put(BlockPos.asLong(x, y, z), state);
			}
		}

		return new StructureHolder(blocks, new Vec3i(sizeX, sizeY, sizeZ));
	}

	private static StructureHolder readVStruct2(ByteReader in) throws IOException {
		int sizeX = in.varInt();
		int sizeY = in.varInt();
		int sizeZ = in.varInt();
		int totalBlocks = in.varInt();

		if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0 || totalBlocks <= 0) {
			return StructureHolder.EMPTY;
		}

		if ((long) sizeX * sizeY * sizeZ > Integer.MAX_VALUE) {
			throw new IOException("Structure too large: " + sizeX + "x" + sizeY + "x" + sizeZ);
		}

		int globalCount = in.varInt();

		if (globalCount < 0 || globalCount > 1_000_000) {
			throw new IOException("Invalid palette size " + globalCount);
		}

		var global = new BlockState[globalCount];

		for (int i = 0; i < globalCount; i++) {
			var state = parseBlockString(in.string());
			int propCount = in.varInt();

			for (int p = 0; p < propCount; p++) {
				var propertyName = in.string();
				var valueName = in.string();
				var property = state.getBlock().getStateDefinition().getProperty(propertyName);

				if (property != null) {
					state = applyProperty(state, property, valueName);
				}
			}

			global[i] = state;
		}

		int sectionCount = in.varInt();

		if (sectionCount < 0 || sectionCount > 16_777_216) {
			throw new IOException("Invalid section count " + sectionCount);
		}

		var blocks = new Long2ObjectOpenHashMap<BlockState>(Math.max(16, Math.min(totalBlocks, 1 << 22)));

		for (int s = 0; s < sectionCount; s++) {
			int sectionX = in.varInt();
			int sectionY = in.varInt();
			int sectionZ = in.varInt();

			if (Math.abs(sectionX) > 1_000_000 || Math.abs(sectionY) > 1_000_000 || Math.abs(sectionZ) > 1_000_000) {
				throw new IOException("Invalid section coords " + sectionX + "/" + sectionY + "/" + sectionZ);
			}

			int baseX = sectionX << 4;
			int baseY = sectionY << 4;
			int baseZ = sectionZ << 4;
			int type = in.u8();

			switch (type) {
				case SECTION_SINGLE -> {
					var state = paletteState(global, in.varInt());

					if (state != null) {
						for (int i = 0; i < 4096; i++) {
							putBlock(blocks, baseX + (i & 15), baseY + ((i >> 8) & 15), baseZ + ((i >> 4) & 15), state);
						}
					}
				}
				case SECTION_SPARSE -> {
					int paletteSize = in.varInt();

					if (paletteSize < 1 || paletteSize > 4096) {
						throw new IOException("Invalid local palette size " + paletteSize);
					}

					var local = new BlockState[paletteSize];

					for (int i = 0; i < paletteSize; i++) {
						local[i] = paletteState(global, in.varInt());
					}

					int count = in.varInt();

					if (count < 0 || count > 4096) {
						throw new IOException("Invalid block count " + count);
					}

					int index = 0;

					for (int i = 0; i < count; i++) {
						index += in.varInt();

						if (index < 0 || index >= 4096) {
							throw new IOException("Invalid block index " + index);
						}

						int localIndex = in.varInt();

						if (localIndex < 0 || localIndex >= paletteSize) {
							throw new IOException("Invalid local palette index " + localIndex);
						}

						var state = local[localIndex];

						if (state != null) {
							putBlock(blocks, baseX + (index & 15), baseY + ((index >> 8) & 15), baseZ + ((index >> 4) & 15), state);
						}
					}
				}
				case SECTION_DENSE -> {
					int paletteSize = in.varInt();

					if (paletteSize < 1 || paletteSize > 4097) {
						throw new IOException("Invalid local palette size " + paletteSize);
					}

					var local = new BlockState[paletteSize];

					for (int i = 0; i < paletteSize; i++) {
						local[i] = paletteState(global, in.varInt());
					}

					var reader = new BitReader(in, bits(paletteSize));

					for (int i = 0; i < 4096; i++) {
						int localIndex = reader.read();

						if (localIndex < 0 || localIndex >= paletteSize) {
							throw new IOException("Invalid local palette index " + localIndex);
						}

						var state = local[localIndex];

						if (state != null) {
							putBlock(blocks, baseX + (i & 15), baseY + ((i >> 8) & 15), baseZ + ((i >> 4) & 15), state);
						}
					}
				}
				default -> throw new IOException("Unknown section type " + type);
			}
		}

		return new StructureHolder(blocks, new Vec3i(sizeX, sizeY, sizeZ));
	}

	public static byte[] toVStructBytes(StructureHolder holder) throws IOException {
		var size = holder.size();
		var out = new ByteWriter();
		out.bytes(VSTRUCT_MAGIC);
		out.varInt(VSTRUCT_VERSION);
		out.varInt(size.getX());
		out.varInt(size.getY());
		out.varInt(size.getZ());

		var counts = new Reference2IntOpenHashMap<BlockState>();

		for (var state : holder.blocks().values()) {
			if (!state.isAir()) {
				counts.addTo(state, 1);
			}
		}

		var global = new ArrayList<BlockState>(counts.keySet());
		global.sort(Comparator.comparingInt((BlockState state) -> -counts.getInt(state)).thenComparing(StructureIO::blockId));
		var globalIds = new Reference2IntOpenHashMap<BlockState>();
		globalIds.defaultReturnValue(-1);

		for (int i = 0; i < global.size(); i++) {
			globalIds.put(global.get(i), i);
		}

		int totalBlocks = 0;

		for (int count : counts.values()) {
			totalBlocks += count;
		}

		out.varInt(totalBlocks);
		out.varInt(global.size());

		for (var state : global) {
			out.string(blockId(state));
			var defaultState = state.getBlock().defaultBlockState();
			var properties = new ArrayList<Property<?>>();

			for (var property : state.getProperties()) {
				if (!state.getValue(property).equals(defaultState.getValue(property))) {
					properties.add(property);
				}
			}

			out.varInt(properties.size());

			for (var property : properties) {
				out.string(property.getName());
				out.string(propertyValueName(property, state));
			}
		}

		var sections = new Long2ObjectOpenHashMap<Section>();

		for (var entry : holder.blocks().long2ObjectEntrySet()) {
			var state = entry.getValue();

			if (state.isAir()) {
				continue;
			}

			long pos = entry.getLongKey();
			int x = BlockPos.getX(pos);
			int y = BlockPos.getY(pos);
			int z = BlockPos.getZ(pos);
			int sectionX = x >> 4;
			int sectionY = y >> 4;
			int sectionZ = z >> 4;
			sections.computeIfAbsent(SectionPos.asLong(sectionX, sectionY, sectionZ), key -> new Section(sectionX, sectionY, sectionZ))
				.blocks().put((short) (((y & 15) << 8) | ((z & 15) << 4) | (x & 15)), state);
		}

		var sectionKeys = sections.keySet().toLongArray();
		Arrays.sort(sectionKeys);
		out.varInt(sectionKeys.length);

		for (long sectionKey : sectionKeys) {
			var section = sections.get(sectionKey);
			writeSection(out, section.x(), section.y(), section.z(), section.blocks(), globalIds);
		}

		return compress(out.toByteArray());
	}

	private static void writeSection(ByteWriter out, int sectionX, int sectionY, int sectionZ, Short2ObjectMap<BlockState> section, Reference2IntOpenHashMap<BlockState> globalIds) {
		out.varInt(sectionX);
		out.varInt(sectionY);
		out.varInt(sectionZ);

		int count = section.size();
		var localIds = new Reference2IntOpenHashMap<BlockState>();
		localIds.defaultReturnValue(-1);
		var localStates = new ArrayList<BlockState>(8);

		for (var state : section.values()) {
			if (localIds.getInt(state) == -1) {
				localIds.put(state, localStates.size());
				localStates.add(state);
			}
		}

		if (count == 4096 && localStates.size() == 1) {
			out.u8(SECTION_SINGLE);
			out.varInt(globalIds.getInt(localStates.getFirst()) + 1);
			return;
		}

		int densePaletteSize = localStates.size() + 1;
		int denseBits = bits(densePaletteSize);
		int denseCost = (4096 * denseBits + 7) / 8 + densePaletteSize * 2;
		int sparseCost = count * 2;

		if (denseCost < sparseCost) {
			out.u8(SECTION_DENSE);
			out.varInt(densePaletteSize);
			out.varInt(0);

			for (var state : localStates) {
				out.varInt(globalIds.getInt(state) + 1);
			}

			var writer = new BitWriter(out);

			for (int i = 0; i < 4096; i++) {
				var state = section.get((short) i);
				writer.write(state == null ? 0 : localIds.getInt(state) + 1, denseBits);
			}

			writer.flush();
		} else {
			out.u8(SECTION_SPARSE);
			out.varInt(localStates.size());

			for (var state : localStates) {
				out.varInt(globalIds.getInt(state) + 1);
			}

			var positions = section.keySet().toShortArray();
			Arrays.sort(positions);
			out.varInt(positions.length);
			int previous = 0;

			for (short position : positions) {
				int index = position & 0xFFFF;
				out.varInt(index - previous);
				previous = index;
				out.varInt(localIds.getInt(section.get(position)));
			}
		}
	}

	public static void writeVStruct(StructureHolder holder, Path path) throws IOException {
		var parent = path.getParent();

		if (parent != null) {
			Files.createDirectories(parent);
		}

		Files.write(path, toVStructBytes(holder));
	}

	// ==== Sponge schematic (.schem) ====

	public static StructureHolder readSchem(byte[] raw) throws IOException {
		var root = NbtIo.readCompressed(new ByteArrayInputStream(raw), NbtAccounter.unlimitedHeap());
		var schematic = root.getCompoundOrEmpty("Schematic");

		if (schematic.isEmpty()) {
			schematic = root;
		}

		int width = schematic.getShortOr("Width", (short) 0) & 0xFFFF;
		int height = schematic.getShortOr("Height", (short) 0) & 0xFFFF;
		int length = schematic.getShortOr("Length", (short) 0) & 0xFFFF;

		if (width <= 0 || height <= 0 || length <= 0) {
			return StructureHolder.EMPTY;
		}

		if ((long) width * height * length > Integer.MAX_VALUE) {
			throw new IOException("Schematic too large: " + width + "x" + height + "x" + length);
		}

		var blocksTag = schematic.getCompoundOrEmpty("Blocks");
		boolean v3 = !blocksTag.isEmpty();
		var paletteTag = v3 ? blocksTag.getCompoundOrEmpty("Palette") : schematic.getCompoundOrEmpty("Palette");

		if (paletteTag.isEmpty()) {
			return StructureHolder.EMPTY;
		}

		byte[] data = v3 ? blocksTag.getByteArray("Data").orElse(null) : schematic.getByteArray("BlockData").orElse(null);
		int[] intData = null;

		if (data == null) {
			intData = v3 ? blocksTag.getIntArray("Data").orElse(null) : schematic.getIntArray("BlockData").orElse(null);
		}

		if (data == null && intData == null) {
			return StructureHolder.EMPTY;
		}

		int maxIndex = -1;

		for (var key : paletteTag.keySet()) {
			maxIndex = Math.max(maxIndex, paletteTag.getIntOr(key, -1));
		}

		if (maxIndex < 0) {
			return StructureHolder.EMPTY;
		}

		var palette = new BlockState[maxIndex + 1];

		for (var key : paletteTag.keySet()) {
			int index = paletteTag.getIntOr(key, -1);

			if (index >= 0 && index < palette.length) {
				palette[index] = parseBlockString(key);
			}
		}

		var blocks = new Long2ObjectOpenHashMap<BlockState>();
		int x = 0;
		int y = 0;
		int z = 0;

		if (data != null) {
			var in = new ByteReader(data);

			while (y < height && in.remaining() > 0) {
				putSchemBlock(blocks, palette, in.varInt(), x, y, z);

				if (++x >= width) {
					x = 0;

					if (++z >= length) {
						z = 0;
						y++;
					}
				}
			}
		} else {
			for (int i = 0; i < intData.length && y < height; i++) {
				putSchemBlock(blocks, palette, intData[i], x, y, z);

				if (++x >= width) {
					x = 0;

					if (++z >= length) {
						z = 0;
						y++;
					}
				}
			}
		}

		return blocks.isEmpty() ? StructureHolder.EMPTY : new StructureHolder(blocks, new Vec3i(width, height, length));
	}

	public static byte[] toSchemBytes(StructureHolder holder) throws IOException {
		var size = holder.size();
		int width = size.getX();
		int height = size.getY();
		int length = size.getZ();

		if (width <= 0 || height <= 0 || length <= 0) {
			throw new IOException("Cannot save an empty structure");
		}

		if (width > 0xFFFF || height > 0xFFFF || length > 0xFFFF) {
			throw new IOException("Structure too large for .schem: " + width + "x" + height + "x" + length);
		}

		if ((long) width * height * length > Integer.MAX_VALUE) {
			throw new IOException("Structure too large for .schem: " + width + "x" + height + "x" + length);
		}

		var keys = new LinkedHashMap<String, Integer>();
		keys.put("minecraft:air", 0);
		var stateIds = new HashMap<BlockState, Integer>();

		for (var state : holder.blocks().values()) {
			if (state.isAir() || stateIds.containsKey(state)) {
				continue;
			}

			var key = BlockUtils.toString(state);
			var index = keys.get(key);

			if (index == null) {
				index = keys.size();
				keys.put(key, index);
			}

			stateIds.put(state, index);
		}

		var data = new ByteWriter();
		int air = 0;

		for (int y = 0; y < height; y++) {
			for (int z = 0; z < length; z++) {
				for (int x = 0; x < width; x++) {
					var state = holder.blocks().get(BlockPos.asLong(x, y, z));
					data.varInt(state == null || state.isAir() ? air : stateIds.getOrDefault(state, air));
				}
			}
		}

		var root = new CompoundTag();
		var schematic = new CompoundTag();
		root.put("Schematic", schematic);
		schematic.putInt("Version", 3);
		NbtUtils.addCurrentDataVersion(schematic);
		schematic.putShort("Width", (short) width);
		schematic.putShort("Height", (short) height);
		schematic.putShort("Length", (short) length);
		schematic.put("Offset", new IntArrayTag(new int[]{0, 0, 0}));

		var blocksTag = new CompoundTag();
		var paletteTag = new CompoundTag();

		for (var entry : keys.entrySet()) {
			paletteTag.putInt(entry.getKey(), entry.getValue());
		}

		blocksTag.put("Palette", paletteTag);
		blocksTag.put("Data", new ByteArrayTag(data.toByteArray()));
		blocksTag.put("BlockEntities", new ListTag());
		schematic.put("Blocks", blocksTag);

		var out = new ByteArrayOutputStream();
		NbtIo.writeCompressed(root, out);
		return out.toByteArray();
	}

	public static void writeSchem(StructureHolder holder, Path path) throws IOException {
		var parent = path.getParent();

		if (parent != null) {
			Files.createDirectories(parent);
		}

		Files.write(path, toSchemBytes(holder));
	}

	private static void putSchemBlock(Long2ObjectOpenHashMap<BlockState> blocks, BlockState[] palette, int index, int x, int y, int z) {
		if (index < 0 || index >= palette.length) {
			return;
		}

		var state = palette[index];

		if (state != null && !state.isAir() && !state.is(Blocks.STRUCTURE_VOID)) {
			blocks.put(BlockPos.asLong(x, y, z), state);
		}
	}

	// ==== vanilla structure NBT ====

	public static List<StructureHolder> readStructureNbt(byte[] raw) throws IOException {
		var tag = NbtIo.readCompressed(new ByteArrayInputStream(raw), NbtAccounter.unlimitedHeap());
		var template = new StructureTemplate();
		template.load(BuiltInRegistries.BLOCK, tag);
		return StructureHolder.allOf(template);
	}

	// ==== helpers ====

	private static void putBlock(Long2ObjectOpenHashMap<BlockState> blocks, int x, int y, int z, BlockState state) {
		blocks.put(BlockPos.asLong(x, y, z), state);
	}

	@Nullable
	private static BlockState paletteState(BlockState[] palette, int encoded) {
		int index = encoded - 1;
		return index >= 0 && index < palette.length ? palette[index] : null;
	}

	private static int bits(int size) {
		return size <= 1 ? 0 : 32 - Integer.numberOfLeadingZeros(size - 1);
	}

	private static String blockId(BlockState state) {
		var id = state.getBlock().builtInRegistryHolder().key().identifier();
		return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
	}

	private static String propertyValueName(Property<?> property, BlockState state) {
		return propertyValueName0(property, state);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static String propertyValueName0(Property property, BlockState state) {
		return property.getName(state.getValue(property));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static BlockState applyProperty(BlockState state, Property property, String valueName) {
		var value = property.getValue(valueName);
		return value.isEmpty() ? state : state.setValue(property, (Comparable) value.get());
	}

	private static BlockState parseBlockString(String value) {
		var id = value.indexOf(':') < 0 ? "minecraft:" + value : value;

		if (id.indexOf(';') >= 0) {
			id = id.replace(';', ',');
		}

		try {
			return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, id, false).blockState();
		} catch (Exception ex) {
			VidLib.LOGGER.warn("Failed to parse block state '" + id + "'");
			return Blocks.AIR.defaultBlockState();
		}
	}

	private static byte[] compress(byte[] raw) throws IOException {
		var out = new ByteArrayOutputStream(Math.max(64, raw.length / 3));

		try (var stream = CompressionMethod.ZSTD.out(out)) {
			stream.write(raw);
		}

		return out.toByteArray();
	}

	private static byte[] decompress(byte[] raw) throws IOException {
		if (raw.length >= 2 && (raw[0] & 0xFF) == 0x1F && (raw[1] & 0xFF) == 0x8B) {
			try (var stream = CompressionMethod.GZIP.in(new ByteArrayInputStream(raw))) {
				return stream.readAllBytes();
			}
		}

		if (raw.length >= 4 && (raw[0] & 0xFF) == 0x28 && (raw[1] & 0xFF) == 0xB5 && (raw[2] & 0xFF) == 0x2F && (raw[3] & 0xFF) == 0xFD) {
			try (var stream = CompressionMethod.ZSTD.in(new ByteArrayInputStream(raw))) {
				return stream.readAllBytes();
			}
		}

		return raw;
	}

	private record Section(int x, int y, int z, Short2ObjectMap<BlockState> blocks) {
		private Section(int x, int y, int z) {
			this(x, y, z, new Short2ObjectOpenHashMap<>());
		}
	}

	private static final class ByteReader {
		private final byte[] data;
		private int position;

		private ByteReader(byte[] data) {
			this.data = data;
		}

		private boolean matches(byte[] magic) {
			if (position + magic.length > data.length) {
				return false;
			}

			for (int i = 0; i < magic.length; i++) {
				if (data[position + i] != magic[i]) {
					return false;
				}
			}

			return true;
		}

		private void skip(int amount) {
			position += amount;
		}

		private int remaining() {
			return data.length - position;
		}

		private int u8() throws IOException {
			if (position >= data.length) {
				throw new IOException("Unexpected end of data");
			}

			return data[position++] & 0xFF;
		}

		private int varInt() throws IOException {
			int value = 0;
			int shift = 0;

			while (true) {
				int b = u8();
				value |= (b & 0x7F) << shift;

				if ((b & 0x80) == 0) {
					return value;
				}

				shift += 7;

				if (shift > 35) {
					throw new IOException("VarInt too big");
				}
			}
		}

		private String string() throws IOException {
			int length = varInt();

			if (length < 0 || position + length > data.length) {
				throw new IOException("Invalid string length " + length);
			}

			var string = new String(data, position, length, StandardCharsets.UTF_8);
			position += length;
			return string;
		}
	}

	private static final class ByteWriter {
		private byte[] data = new byte[256];
		private int size;

		private void ensure(int amount) {
			if (size + amount > data.length) {
				data = Arrays.copyOf(data, Math.max(data.length * 2, size + amount));
			}
		}

		private void u8(int value) {
			ensure(1);
			data[size++] = (byte) value;
		}

		private void bytes(byte[] value) {
			ensure(value.length);
			System.arraycopy(value, 0, data, size, value.length);
			size += value.length;
		}

		private void varInt(int value) {
			while ((value & ~0x7F) != 0) {
				u8((value & 0x7F) | 0x80);
				value >>>= 7;
			}

			u8(value);
		}

		private void string(String value) {
			var bytes = value.getBytes(StandardCharsets.UTF_8);
			varInt(bytes.length);
			bytes(bytes);
		}

		private byte[] toByteArray() {
			return Arrays.copyOf(data, size);
		}
	}

	private static final class BitWriter {
		private final ByteWriter out;
		private int current;
		private int used;

		private BitWriter(ByteWriter out) {
			this.out = out;
		}

		private void write(int value, int bits) {
			for (int i = 0; i < bits; i++) {
				if (((value >>> i) & 1) != 0) {
					current |= 1 << used;
				}

				if (++used == 8) {
					out.u8(current);
					current = 0;
					used = 0;
				}
			}
		}

		private void flush() {
			if (used > 0) {
				out.u8(current);
				current = 0;
				used = 0;
			}
		}
	}

	private static final class BitReader {
		private final ByteReader in;
		private final int bits;
		private int current;
		private int remaining;

		private BitReader(ByteReader in, int bits) {
			this.in = in;
			this.bits = bits;
		}

		private int read() throws IOException {
			int value = 0;

			for (int i = 0; i < bits; i++) {
				if (remaining == 0) {
					current = in.u8();
					remaining = 8;
				}

				value |= (current & 1) << i;
				current >>>= 1;
				remaining--;
			}

			return value;
		}
	}
}