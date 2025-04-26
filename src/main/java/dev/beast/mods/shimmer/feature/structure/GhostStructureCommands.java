package dev.beast.mods.shimmer.feature.structure;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.util.UndashedUuid;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public interface GhostStructureCommands {
	int CHUNK_SIZE = 6;
	int CHUNK_OFFSET = (1 << CHUNK_SIZE) - 1;

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("ghost-structure", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("capture")
			.then(Commands.argument("name", StringArgumentType.word())
				.then(Commands.argument("start", BlockPosArgument.blockPos())
					.then(Commands.argument("end", BlockPosArgument.blockPos())
						.executes(ctx -> capture(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), BlockPosArgument.getBlockPos(ctx, "start"), BlockPosArgument.getBlockPos(ctx, "end"), null))
						.then(Commands.argument("filter", BlockFilter.KNOWN_CODEC.argument(buildContext))
							.executes(ctx -> capture(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), BlockPosArgument.getBlockPos(ctx, "start"), BlockPosArgument.getBlockPos(ctx, "end"), BlockFilter.KNOWN_CODEC.get(ctx, "filter")))
						)
					)
				)
			)
		)
		.then(Commands.literal("save")
			.then(Commands.argument("name", StringArgumentType.word())
				.then(Commands.argument("start", BlockPosArgument.blockPos())
					.then(Commands.argument("end", BlockPosArgument.blockPos())
						.executes(ctx -> save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), BlockPosArgument.getBlockPos(ctx, "start"), BlockPosArgument.getBlockPos(ctx, "end"), false, null))
						.then(Commands.argument("shell", BoolArgumentType.bool())
							.executes(ctx -> save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), BlockPosArgument.getBlockPos(ctx, "start"), BlockPosArgument.getBlockPos(ctx, "end"), BoolArgumentType.getBool(ctx, "shell"), null))
							.then(Commands.argument("filter", BlockFilter.KNOWN_CODEC.argument(buildContext))
								.executes(ctx -> save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), BlockPosArgument.getBlockPos(ctx, "start"), BlockPosArgument.getBlockPos(ctx, "end"), BoolArgumentType.getBool(ctx, "shell"), BlockFilter.KNOWN_CODEC.get(ctx, "filter")))
							)
						)
					)
				)
			)
		)
	);

	static int capture(CommandSourceStack source, String name, BlockPos start, BlockPos end, @Nullable BlockFilter filter) {
		var fname = name.replace('-', '_').replaceAll("\\W+", "_");

		try {
			long startTime = System.currentTimeMillis();
			var minX = Math.min(start.getX(), end.getX());
			var minY = Math.min(start.getY(), end.getY());
			var minZ = Math.min(start.getZ(), end.getZ());
			var maxX = Math.max(start.getX(), end.getX());
			var maxY = Math.max(start.getY(), end.getY());
			var maxZ = Math.max(start.getZ(), end.getZ());
			var volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
			source.sendSuccess(() -> Component.literal("Capturing %,d block area...".formatted(volume)), false);

			var capture = StructureHolder.capture(source.getLevel(), start, end, filter, true).withoutInvisibleBlocks();

			if (capture.empty()) {
				source.sendFailure(Component.literal("No blocks captured!"));
				return 0;
			}

			source.sendSuccess(() -> Component.literal("Found %,d blocks".formatted(capture.blocks().size())), false);

			var shell = capture.visualShell();

			if (shell.empty()) {
				source.sendFailure(Component.literal("Empty shell!"));
				return 0;
			}

			int minChunkX = minX >> CHUNK_SIZE;
			int minChunkY = minY >> CHUNK_SIZE;
			int minChunkZ = minZ >> CHUNK_SIZE;
			int maxChunkX = maxX >> CHUNK_SIZE;
			int maxChunkY = maxY >> CHUNK_SIZE;
			int maxChunkZ = maxZ >> CHUNK_SIZE;

			source.sendSuccess(() -> Component.literal("Slicing into %d x %d x %d chunks...".formatted(maxChunkX - minChunkX + 1, maxChunkY - minChunkY + 1, maxChunkZ - minChunkZ + 1)), false);

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

				var jsonDir = zip.getPath("/assets/video/shimmer/ghost_structure/ghost_chunks/%s".formatted(fname));
				Files.createDirectories(jsonDir);

				for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
					for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
						for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
							int sliceX = chunkX << CHUNK_SIZE;
							int sliceY = chunkY << CHUNK_SIZE;
							int sliceZ = chunkZ << CHUNK_SIZE;
							var slice = shell.slice(sliceX - minX, sliceY - minY, sliceZ - minZ, sliceX - minX + CHUNK_OFFSET, sliceY - minY + CHUNK_OFFSET, sliceZ - minZ + CHUNK_OFFSET);

							if (slice.empty()) {
								continue;
							}

							var posString = UndashedUuid.toString(new UUID(BlockPos.asLong(sliceX, sliceY, sliceZ), BlockPos.asLong(sliceX + CHUNK_OFFSET, sliceY + CHUNK_OFFSET, sliceZ + CHUNK_OFFSET)));

							// NbtIo.writeCompressed(slice.toStructureNBT(), structureDir.resolve("%s.nbt".formatted(posString)));
							slice.toVStruct(structureDir.resolve("%s.vstruct".formatted(posString)));

							Files.writeString(jsonDir.resolve("%s.json".formatted(posString)), """
								{
								 	"structure": {
								 		"id": "video:ghost_chunks/%s/%s",
								 		"center_x": false,
								 		"center_z": false
								 	},
								 	"pos": [%d, %d, %d],
								 	"preload": true,
								 	"inflate": true
								 }""".formatted(fname, posString, minX, minY, minZ));
						}
					}
				}
			}

			long doneTime = (System.currentTimeMillis() - startTime) / 1000L;
			source.sendSuccess(() -> Component.literal("Done in %,d s! Saved as vidlib/%s-ghost-chunks.zip".formatted(doneTime, name)), false);
		} catch (Exception ex) {
			ex.printStackTrace();
			source.sendFailure(Component.literal("Failed to create vidlib/%s-ghost-chunks.zip".formatted(name)));
		}

		return 1;
	}

	static int save(CommandSourceStack source, String name, BlockPos start, BlockPos end, boolean createShell, @Nullable BlockFilter filter) {
		var fname = name.replace('-', '_').replaceAll("\\W+", "_");
		var pathStr = "vidlib/%s.vstruct".formatted(fname);

		try {
			long startTime = System.currentTimeMillis();
			var minX = Math.min(start.getX(), end.getX());
			var minY = Math.min(start.getY(), end.getY());
			var minZ = Math.min(start.getZ(), end.getZ());
			var maxX = Math.max(start.getX(), end.getX());
			var maxY = Math.max(start.getY(), end.getY());
			var maxZ = Math.max(start.getZ(), end.getZ());
			var volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
			source.sendSuccess(() -> Component.literal("Capturing %,d block area...".formatted(volume)), false);

			var capture = StructureHolder.capture(source.getLevel(), start, end, filter, true).withoutInvisibleBlocks();

			if (capture.empty()) {
				source.sendFailure(Component.literal("No blocks captured!"));
				return 0;
			}

			source.sendSuccess(() -> Component.literal("Found %,d blocks".formatted(capture.blocks().size())), false);

			var shell = createShell ? capture.visualShell() : capture;

			if (shell.empty()) {
				source.sendFailure(Component.literal("Empty shell!"));
				return 0;
			}

			var path = FMLPaths.GAMEDIR.get().resolve(pathStr);

			if (Files.notExists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}

			shell.toVStruct(path);

			long doneTime = (System.currentTimeMillis() - startTime) / 1000L;
			source.sendSuccess(() -> Component.literal("Done in %,d s! Saved as %s".formatted(doneTime, pathStr)), false);
		} catch (Exception ex) {
			ex.printStackTrace();
			source.sendFailure(Component.literal("Failed to create %s".formatted(pathStr)));
		}

		return 1;
	}
}
