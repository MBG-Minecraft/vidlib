package dev.latvian.mods.vidlib.feature.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.block.filter.BlockFilter;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.particle.ShapeParticleOptions;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.MessageConsumer;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import imgui.type.ImBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

public interface StructureCapture {
	int CHUNK_SIZE = 6;
	int CHUNK_OFFSET = (1 << CHUNK_SIZE) - 1;

	MutableObject<CurrentStructureCapture> CURRENT = new MutableObject<>(new CurrentStructureCapture());
	MutableObject<BlockPos> POS_1 = new MutableObject<>(null);
	MutableObject<BlockPos> POS_2 = new MutableObject<>(null);
	MutableObject<Ref<BlockFilter>> IGNORE_FILTER = new MutableObject<>(BlockFilter.NONE);
	ShapeParticleOptions PARTICLE = new ShapeParticleOptions(20, Gradient.CYAN, Gradient.EMPTY);
	ImBoolean INCLUDE_FLUIDS = new ImBoolean(true);
	ImBoolean PARTICLES = new ImBoolean(true);

	static BlockFilter buildFilter() {
		var filter = StructureCapture.IGNORE_FILTER.getValue().value().not();

		if (!INCLUDE_FLUIDS.get()) {
			filter = filter.and(BlockFilter.FLUID.value().not());
		}

		return filter;
	}

	static void setPos(int index, BlockPos pos) {
		if (index == 1) {
			POS_1.setValue(pos);
		} else {
			POS_2.setValue(pos);
		}
	}

	static void resetPos() {
		POS_1.setValue(null);
		POS_2.setValue(null);
	}

	static @Nullable CurrentStructureCapture resolveCurrent(Level level, MessageConsumer source) {
		var pos1 = POS_1.getValue();
		var pos2 = POS_2.getValue();

		if (pos1 != null && pos2 != null) {
			var region = new CurrentStructureCapture();
			region.addBlocks(level, source, pos1, pos2);
			return region;
		}

		if (pos1 != null || pos2 != null) {
			source.error(Component.literal("Only pos1 or pos2 is set! Set both to capture from a region, or run /structure reset to clear the positions and use manually captured blocks."));
			return null;
		}

		return CURRENT.getValue();
	}

	static int capture(Level level, MessageConsumer source, String name) {
		var startTime = System.currentTimeMillis();
		var fname = name.replace('-', '_').replaceAll("\\W+", "_");

		try {
			var current = prepare(level, source);

			if (current == null) {
				return 0;
			}

			if (current.blockStructure.empty() && current.waterStructure.empty()) {
				source.error(Component.literal("No blocks captured!"));
				return 0;
			}

			source.tell(Component.literal("Found %,d blocks".formatted(current.blockStructure.blocks().size() + current.waterStructure.blocks().size())));

			var blockStructure = current.blockStructure;
			var fluidStructure = current.waterStructure;

			int minChunkX = current.minX >> CHUNK_SIZE;
			int minChunkY = current.minY >> CHUNK_SIZE;
			int minChunkZ = current.minZ >> CHUNK_SIZE;
			int maxChunkX = current.maxX >> CHUNK_SIZE;
			int maxChunkY = current.maxY >> CHUNK_SIZE;
			int maxChunkZ = current.maxZ >> CHUNK_SIZE;

			source.tell(Component.literal("Slicing into %d x %d x %d chunks...".formatted(maxChunkX - minChunkX + 1, maxChunkY - minChunkY + 1, maxChunkZ - minChunkZ + 1)));

			var path = VidLibPaths.LOCAL.get().resolve("export/%s-ghost-chunks.jar".formatted(name));

			if (Files.notExists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}

			Files.deleteIfExists(path);

			try (var jar = IOUtils.openAsZip(path, Map.of("create", "true"))) {
				Files.writeString(jar.getPath("/pack.mcmeta"), """
					{
						"pack": {
							"pack_format": 8,
							"supported_formats": [8, 9999],
							"description": "§e'%s' Ghost Chunks"
						}
					}""".formatted(name));

				var structureDir = jar.getPath("/assets/video/structure/ghost_chunks/%s".formatted(fname));
				Files.createDirectories(structureDir);

				var jsonDir = jar.getPath("/assets/video/vidlib/ghost_structure/ghost_chunks");
				Files.createDirectories(jsonDir);

				Files.createDirectories(jar.getPath("/META-INF"));

				var json = new JsonObject();
				var strucArr = new JsonArray();

				json.addProperty("ghost_chunks", true);
				json.add("structures", strucArr);
				json.addProperty("animation_ticks", 0D);

				var locArr = new JsonArray();
				var loc = new JsonArray();
				loc.add(current.minX);
				loc.add(current.minY);
				loc.add(current.minZ);
				locArr.add(loc);
				json.add("locations", locArr);
				json.addProperty("preload", true);

				if (!fluidStructure.empty()) {
					fluidStructure.toVStruct(structureDir.resolve("fluids.vstruct"));
					addPart(strucArr, fname, "fluids", bounds(fluidStructure));
				}

				for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
					for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
						for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
							int sliceX = chunkX << CHUNK_SIZE;
							int sliceY = chunkY << CHUNK_SIZE;
							int sliceZ = chunkZ << CHUNK_SIZE;

							var slice = blockStructure.slice(sliceX - current.minX, sliceY - current.minY, sliceZ - current.minZ, sliceX - current.minX + CHUNK_OFFSET, sliceY - current.minY + CHUNK_OFFSET, sliceZ - current.minZ + CHUNK_OFFSET);

							if (slice.empty()) {
								continue;
							}

							var posString = UndashedUuid.toString(new UUID(BlockPos.asLong(sliceX, sliceY, sliceZ), BlockPos.asLong(sliceX + CHUNK_OFFSET, sliceY + CHUNK_OFFSET, sliceZ + CHUNK_OFFSET)));
							slice.toVStruct(structureDir.resolve("%s.vstruct".formatted(posString)));
							addPart(strucArr, fname, posString, bounds(slice));
						}
					}
				}

				Files.writeString(jsonDir.resolve("%s.json".formatted(fname)), JsonUtils.string(json));

				Files.writeString(jar.getPath("/META-INF/neoforge.mods.toml"), """
					modLoader = "lowcodefml"
					loaderVersion = "[1,)"
					license = "ARR"
					
					[[mods]]
					modId = "%s_ghost_chunks"
					namespace = "video"
					displayName = "'%s' Ghost Chunks"
					description = '''VidLib Ghost Chunks'''
					""".formatted(fname, name));
			}

			long doneTime = (System.currentTimeMillis() - startTime) / 1000L;
			source.tell(Component.literal("Done in %,d s! Saved as local/vidlib/export/%s-ghost-chunks.jar".formatted(doneTime, name)));
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to create ghost chunks jar " + name, ex);
			source.error(Component.literal("Failed to create vidlib/%s-ghost-chunks.jar".formatted(name)));
		}

		return 1;
	}

	private static void addPart(JsonArray structures, String fname, String name, AAIBB bounds) {
		var part = new JsonObject();
		part.addProperty("structure", "video:ghost_chunks/%s/%s".formatted(fname, name));

		var boundsArr = new JsonArray();
		boundsArr.add(bounds.minX());
		boundsArr.add(bounds.minY());
		boundsArr.add(bounds.minZ());
		boundsArr.add(bounds.maxX());
		boundsArr.add(bounds.maxY());
		boundsArr.add(bounds.maxZ());
		part.add("bounds", boundsArr);
		structures.add(part);
	}

	private static AAIBB bounds(StructureHolder holder) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;

		for (var pos : holder.blocks().keySet()) {
			minX = Math.min(minX, BlockPos.getX(pos));
			minY = Math.min(minY, BlockPos.getY(pos));
			minZ = Math.min(minZ, BlockPos.getZ(pos));
			maxX = Math.max(maxX, BlockPos.getX(pos));
			maxY = Math.max(maxY, BlockPos.getY(pos));
			maxZ = Math.max(maxZ, BlockPos.getZ(pos));
		}

		return new AAIBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	static int save(Level level, MessageConsumer source, String name, boolean createShell) {
		return save(level, source, name, createShell, false);
	}

	static int saveSchem(Level level, MessageConsumer source, String name) {
		return save(level, source, name, false, true);
	}

	static int save(Level level, MessageConsumer source, String name, boolean createShell, boolean schem) {
		long startTime = System.currentTimeMillis();
		var fname = name.replace('-', '_').replaceAll("\\W+", "_");
		var pathStr = "%s%s".formatted(fname, schem ? ".schem" : ".vstruct");

		try {
			var current = prepare(level, source);

			if (current == null) {
				return 0;
			}

			if (current.blockStructure.empty()) {
				source.error(Component.literal("No blocks captured!"));
				return 0;
			}

			source.tell(Component.literal("Found %,d blocks".formatted(current.blockStructure.blocks().size())));

			var finalStructure = createShell ? current.blockStructure.visualShell() : current.blockStructure;

			if (finalStructure.empty()) {
				source.error(Component.literal("Empty structure!"));
				return 0;
			}

			var path = VidLibPaths.GAME.get().resolve(pathStr);

			if (schem) {
				finalStructure.toSchem(path);
			} else {
				finalStructure.toVStruct(path);
			}

			long doneTime = (System.currentTimeMillis() - startTime) / 1000L;
			source.tell(Component.literal("Done in %,d s! Saved as %s".formatted(doneTime, pathStr)));
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to save structure " + pathStr, ex);
			source.error(Component.literal("Failed to create %s".formatted(pathStr)));
		}

		return 1;
	}

	@Nullable
	private static CurrentStructureCapture prepare(Level level, MessageConsumer source) {
		var current = resolveCurrent(level, source);

		if (current != null) {
			current.build(source);
		}

		return current;
	}

	static int move(BlockPos pos) {
		CURRENT.getValue().move(pos);
		return 1;
	}
}