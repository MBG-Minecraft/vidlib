package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public interface ZipCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("zip", (command, buildContext) -> command
		.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
		.executes(ctx -> zip(ctx.getSource()))
	);

	private static void zipDirectory(Path sourceDir, Path zipFilePath) throws IOException {
		try (var zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
			Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
				@Override
				public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
					var entry = new ZipEntry(sourceDir.relativize(file).toString().replace('\\', '/'));
					zos.putNextEntry(entry);
					Files.copy(file, zos);
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	static CompletableFuture<String> zip(MinecraftServer server) {
		for (var level : server.getAllLevels()) {
			if (level != null) {
				level.noSave = true;
			}
		}

		server.saveEverything(true, true, true);

		return CompletableFuture.supplyAsync(() -> {
			try {
				var name = CommonGameEngine.INSTANCE.getBackupInfo(server);
				var from = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().toRealPath();
				var fromName = from.getFileName().toString();
				var to = from.resolveSibling(fromName + "-" + name);
				var toName = to.getFileName().toString();
				var zipFile = from.resolveSibling(toName + ".zip");

				try {
					var process = new ProcessBuilder(Util.getPlatform() == Util.OS.WINDOWS ? List.of("robocopy", fromName, toName, "/E", "/ZB", "/COPYALL", "/MT:16") : List.of("cp", "-R", fromName, toName))
						.directory(from.getParent().toAbsolutePath().toFile())
						.start();

					process.waitFor(1L, TimeUnit.MINUTES);
					VidLib.LOGGER.info("Backup status: " + process.exitValue());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				zipDirectory(to, zipFile);
				Files.delete(to);

				return zipFile.getFileName().toString();
			} catch (Throwable ex) {
				ex.printStackTrace();
			} finally {
				server.execute(() -> {
					for (var level : server.getAllLevels()) {
						if (level != null) {
							level.noSave = false;
						}
					}
				});
			}

			throw new IllegalStateException("Failed to create a backup");
		});
	}

	static int zip(CommandSourceStack source) {
		var start = System.currentTimeMillis();
		source.broadcast("Creating a world backup...");
		zip(source.getServer()).thenAccept(name -> source.sendSuccess(() -> Component.literal("Saved a backup of '%s' (%.01f s)".formatted(name, (System.currentTimeMillis() - start) / 1000F)), true));
		return 1;
	}
}
