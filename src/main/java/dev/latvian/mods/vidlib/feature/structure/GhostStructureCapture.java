package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.particle.CubeParticleOptions;
import dev.latvian.mods.vidlib.util.MessageConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
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
	MutableObject<BlockFilter> FILTER = new MutableObject<>(BlockFilter.ANY.instance());
	CubeParticleOptions PARTICLE = new CubeParticleOptions(Color.CYAN, Color.TRANSPARENT, 20);

	static int capture(MessageConsumer source, String name) {
		var startTime = System.currentTimeMillis();
		var fname = name.replace('-', '_').replaceAll("\\W+", "_");

		try {
			var current = CURRENT.getValue();
			current.build(source);

			if (current.structure.empty()) {
				source.error(Component.literal("No blocks captured!"));
				return 0;
			}

			source.tell(Component.literal("Found %,d blocks".formatted(current.structure.blocks().size())));

			var shell = current.structure.visualShell();

			if (shell.empty()) {
				source.error(Component.literal("Empty shell!"));
				return 0;
			}

			int minChunkX = current.minX >> CHUNK_SIZE;
			int minChunkY = current.minY >> CHUNK_SIZE;
			int minChunkZ = current.minZ >> CHUNK_SIZE;
			int maxChunkX = current.maxX >> CHUNK_SIZE;
			int maxChunkY = current.maxY >> CHUNK_SIZE;
			int maxChunkZ = current.maxZ >> CHUNK_SIZE;

			source.tell(Component.literal("Slicing into %d x %d x %d chunks...".formatted(maxChunkX - minChunkX + 1, maxChunkY - minChunkY + 1, maxChunkZ - minChunkZ + 1)));

			var path = FMLPaths.GAMEDIR.get().resolve("vidlib/%s-ghost-chunks.zip".formatted(name));

			if (Files.notExists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}

			Files.deleteIfExists(path);

			try (var zip = FileSystems.newFileSystem(new URI("jar:file", path.toUri().getPath(), null), Map.of("create", "true"))) {
				Files.writeString(zip.getPath("/pack.mcmeta"), """
					{
						"pack": {
							"pack_format": 8,
							"supported_formats": [8, 9999],
							"description": "§e'%s' Ghost Chunks"
						}
					}""".formatted(name));

				var structureDir = zip.getPath("/assets/video/structure/ghost_chunks/%s".formatted(fname));
				Files.createDirectories(structureDir);

				var jsonDir = zip.getPath("/assets/video/vidlib/ghost_structure/ghost_chunks/%s".formatted(fname));
				Files.createDirectories(jsonDir);

				var locDir = zip.getPath("/assets/video/vidlib/location/ghost_chunks");
				Files.createDirectories(locDir);

				Files.writeString(locDir.resolve("%s.json".formatted(fname)), """
					{
					 	"position": [%d, %d, %d]
					}""".formatted(current.minX, current.minY, current.minZ));

				for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
					for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
						for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
							int sliceX = chunkX << CHUNK_SIZE;
							int sliceY = chunkY << CHUNK_SIZE;
							int sliceZ = chunkZ << CHUNK_SIZE;

							var slice = shell.slice(sliceX - current.minX, sliceY - current.minY, sliceZ - current.minZ, sliceX - current.minX + CHUNK_OFFSET, sliceY - current.minY + CHUNK_OFFSET, sliceZ - current.minZ + CHUNK_OFFSET);

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

							Files.writeString(jsonDir.resolve("%s.json".formatted(posString)), """
								{
								 	"structures": [{
								 		"id": "video:ghost_chunks/%s/%s",
								 		"center_x": false,
								 		"center_z": false
								 	}],
								 	"locations": ["video:ghost_chunks/%s"],
								 	"slice": [%d, %d, %d, %d, %d, %d],
								 	"preload": true,
								 	"inflate": true
								 }""".formatted(fname, posString, fname, rMinSliceX, rMinSliceY, rMinSliceZ, rMaxSliceX, rMaxSliceY, rMaxSliceZ));
						}
					}
				}
			}

			long doneTime = (System.currentTimeMillis() - startTime) / 1000L;
			source.tell(Component.literal("Done in %,d s! Saved as vidlib/%s-ghost-chunks.zip".formatted(doneTime, name)));
		} catch (Exception ex) {
			ex.printStackTrace();
			source.error(Component.literal("Failed to create vidlib/%s-ghost-chunks.zip".formatted(name)));
		}

		return 1;
	}

	static int save(MessageConsumer source, String name, boolean createShell) {
		long startTime = System.currentTimeMillis();
		var fname = name.replace('-', '_').replaceAll("\\W+", "_");
		var pathStr = "vidlib/%s.vstruct".formatted(fname);

		try {
			var current = CURRENT.getValue();
			current.build(source);

			if (current.structure.empty()) {
				source.error(Component.literal("No blocks captured!"));
				return 0;
			}

			source.tell(Component.literal("Found %,d blocks".formatted(current.structure.blocks().size())));

			var finalStructure = createShell ? current.structure.visualShell() : current.structure;

			if (finalStructure.empty()) {
				source.error(Component.literal("Empty structure!"));
				return 0;
			}

			var path = FMLPaths.GAMEDIR.get().resolve(pathStr);

			if (Files.notExists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}

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
