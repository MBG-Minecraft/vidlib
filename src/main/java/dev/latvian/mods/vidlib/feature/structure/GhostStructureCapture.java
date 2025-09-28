package dev.latvian.mods.vidlib.feature.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.MessageConsumer;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.particle.ShapeParticleOptions;
import dev.latvian.mods.vidlib.util.JsonUtils;
import imgui.type.ImBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.mutable.MutableObject;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

public interface GhostStructureCapture {
	int CHUNK_SIZE = 6;
	int CHUNK_OFFSET = (1 << CHUNK_SIZE) - 1;

	MutableObject<CurrentGhostStructureCapture> CURRENT = new MutableObject<>(new CurrentGhostStructureCapture());
	MutableObject<BlockFilter> IGNORE_FILTER = new MutableObject<>(BlockFilter.NONE.instance());
	ShapeParticleOptions PARTICLE = new ShapeParticleOptions(20, Color.CYAN, Color.TRANSPARENT);
	ImBoolean INCLUDE_FLUIDS = new ImBoolean(true);
	ImBoolean PARTICLES = new ImBoolean(true);

	static BlockFilter buildFilter() {
		var filter = GhostStructureCapture.IGNORE_FILTER.getValue().not();

		if (!INCLUDE_FLUIDS.get()) {
			filter = filter.and(BlockFilter.FLUID.instance().not());
		}

		return filter;
	}

	static int capture(MessageConsumer source, String name) {
		var startTime = System.currentTimeMillis();
		var fname = name.replace('-', '_').replaceAll("\\W+", "_");

		try {
			var current = CURRENT.getValue();
			current.build(source);

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

			var path = VidLibPaths.LOCAL.resolve("export/%s-ghost-chunks.jar".formatted(name));

			if (Files.notExists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}

			Files.deleteIfExists(path);

			try (var jar = FileSystems.newFileSystem(new URI("jar:file", path.toUri().getPath(), null), Map.of("create", "true"))) {
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

				var metaInfDir = jar.getPath("/META-INF");
				Files.createDirectories(metaInfDir);

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
					int rMinSliceX = Integer.MAX_VALUE;
					int rMinSliceY = Integer.MAX_VALUE;
					int rMinSliceZ = Integer.MAX_VALUE;
					int rMaxSliceX = Integer.MIN_VALUE;
					int rMaxSliceY = Integer.MIN_VALUE;
					int rMaxSliceZ = Integer.MIN_VALUE;

					for (var entry : fluidStructure.blocks().long2ObjectEntrySet()) {
						var pos = BlockPos.of(entry.getLongKey());
						rMinSliceX = Math.min(rMinSliceX, pos.getX());
						rMinSliceY = Math.min(rMinSliceY, pos.getY());
						rMinSliceZ = Math.min(rMinSliceZ, pos.getZ());
						rMaxSliceX = Math.max(rMaxSliceX, pos.getX());
						rMaxSliceY = Math.max(rMaxSliceY, pos.getY());
						rMaxSliceZ = Math.max(rMaxSliceZ, pos.getZ());
					}

					fluidStructure.toVStruct(structureDir.resolve("fluids.vstruct"));

					var str = new JsonObject();
					str.addProperty("structure", "video:ghost_chunks/%s/%s".formatted(fname, "fluids"));

					var boundsArr = new JsonArray();
					boundsArr.add(rMinSliceX);
					boundsArr.add(rMinSliceY);
					boundsArr.add(rMinSliceZ);
					boundsArr.add(rMaxSliceX);
					boundsArr.add(rMaxSliceY);
					boundsArr.add(rMaxSliceZ);
					str.add("bounds", boundsArr);
					strucArr.add(str);
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

							int rMinSliceX = Integer.MAX_VALUE;
							int rMinSliceY = Integer.MAX_VALUE;
							int rMinSliceZ = Integer.MAX_VALUE;
							int rMaxSliceX = Integer.MIN_VALUE;
							int rMaxSliceY = Integer.MIN_VALUE;
							int rMaxSliceZ = Integer.MIN_VALUE;

							for (var entry : slice.blocks().long2ObjectEntrySet()) {
								var pos = BlockPos.of(entry.getLongKey());
								rMinSliceX = Math.min(rMinSliceX, pos.getX());
								rMinSliceY = Math.min(rMinSliceY, pos.getY());
								rMinSliceZ = Math.min(rMinSliceZ, pos.getZ());
								rMaxSliceX = Math.max(rMaxSliceX, pos.getX());
								rMaxSliceY = Math.max(rMaxSliceY, pos.getY());
								rMaxSliceZ = Math.max(rMaxSliceZ, pos.getZ());
							}

							var posString = UndashedUuid.toString(new UUID(BlockPos.asLong(sliceX, sliceY, sliceZ), BlockPos.asLong(sliceX + CHUNK_OFFSET, sliceY + CHUNK_OFFSET, sliceZ + CHUNK_OFFSET)));

							slice.toVStruct(structureDir.resolve("%s.vstruct".formatted(posString)));

							var str = new JsonObject();
							str.addProperty("structure", "video:ghost_chunks/%s/%s".formatted(fname, posString));

							var boundsArr = new JsonArray();
							boundsArr.add(rMinSliceX);
							boundsArr.add(rMinSliceY);
							boundsArr.add(rMinSliceZ);
							boundsArr.add(rMaxSliceX);
							boundsArr.add(rMaxSliceY);
							boundsArr.add(rMaxSliceZ);
							str.add("bounds", boundsArr);
							strucArr.add(str);
						}
					}
				}

				Files.writeString(jsonDir.resolve("%s.json".formatted(fname)), JsonUtils.string(json));

				Files.writeString(metaInfDir.resolve("neoforge.mods.toml"), """
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
			source.tell(Component.literal("Done in %,d s! Saved as local/vidlib/export/%s-ghost-chunks.jar".formatted(doneTime, name)).setStyle(Style.EMPTY.withClickToOpen(path.getParent())));
		} catch (Exception ex) {
			ex.printStackTrace();
			source.error(Component.literal("Failed to create vidlib/%s-ghost-chunks.jar".formatted(name)));
		}

		return 1;
	}

	static int save(MessageConsumer source, String name, boolean createShell) {
		long startTime = System.currentTimeMillis();
		var fname = name.replace('-', '_').replaceAll("\\W+", "_");
		var pathStr = "%s.vstruct".formatted(fname);

		try {
			var current = CURRENT.getValue();
			current.build(source);

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

			var path = VidLibPaths.GAME.resolve(pathStr);

			finalStructure.toVStruct(path);

			long doneTime = (System.currentTimeMillis() - startTime) / 1000L;
			source.tell(Component.literal("Done in %,d s! Saved as %s".formatted(doneTime, pathStr)));
		} catch (Exception ex) {
			ex.printStackTrace();
			source.error(Component.literal("Failed to create %s".formatted(pathStr)));
		}

		return 1;
	}

	static int move(BlockPos pos) {
		CURRENT.getValue().move(pos);
		return 1;
	}
}
