package dev.latvian.mods.vidlib.feature.misc.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public interface BackupCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("backup", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("test-compression")
			.executes(ctx -> testCompression(ctx.getSource()))
		)
		.then(Commands.argument("custom-name", StringArgumentType.greedyString())
			.executes(ctx -> backup(ctx.getSource(), StringArgumentType.getString(ctx, "custom-name")))
		)
		.executes(ctx -> backup(ctx.getSource(), ""))
	);

	static CompletableFuture<Path> backup(MinecraftServer server, Path worldPath, Instant now, String customName) {
		PlatformHelper.CURRENT.pauseSaving(server);
		var name = CommonGameEngine.INSTANCE.getFullBackupInfo(server, now, customName);

		try {
			var from = worldPath.toAbsolutePath().toRealPath();
			var fromName = from.getFileName().toString();
			var to = from.resolveSibling(fromName + "-" + name);
			var toName = to.getFileName().toString();

			List<Path> allPaths;

			try (var stream = Files.walk(from)) {
				allPaths = new ArrayList<>(stream.filter(p -> {
					var n = p.getFileName().toString().toLowerCase(Locale.ROOT);
					return !(n.endsWith(".lock") || n.equals("lock"));
				}).sorted().toList());
			}

			return CompletableFuture.supplyAsync(() -> {
				var item = ProgressQueue.queueSingleItem("Copying...");
				item.setSize(0L);
				item.setInfoText(ProgressItemNameFunction.COUNT);

				for (var src : allPaths) {
					if (Files.isDirectory(src)) {
						var dst = to.resolve(from.relativize(src).toString());

						try {
							var attributes = Files.readAttributes(src, BasicFileAttributes.class);
							Files.createDirectory(dst);
							Files.getFileAttributeView(dst, BasicFileAttributeView.class).setTimes(attributes.lastModifiedTime(), attributes.lastAccessTime(), attributes.creationTime());
						} catch (Exception ex) {
							throw new RuntimeException("Error creating directory " + dst, ex);
						}
					} else {
						item.addSize(1L);
					}
				}

				VidLib.LOGGER.info("Directories of " + toName + " created");

				var list = new ArrayList<CompletableFuture<Void>>();

				for (var src : allPaths) {
					if (Files.isRegularFile(src)) {
						var relativePath = from.relativize(src);
						var dst = to.resolve(relativePath);

						list.add(CompletableFuture.runAsync(() -> {
							try {
								Files.copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES);
								item.addProgress(1L);
							} catch (Exception ex) {
								VidLib.LOGGER.error("Error copying " + relativePath, ex);
							}
						}, Util.ioPool()));
					}
				}

				CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
				item.setDone();
				VidLib.LOGGER.info("Files of " + toName + " created");
				server.execute(() -> PlatformHelper.CURRENT.resumeSaving(server));
				return to;
			});
		} catch (Throwable ex) {
			VidLib.LOGGER.error("Error creating a world backup '" + name + "'", ex);
			server.execute(() -> PlatformHelper.CURRENT.resumeSaving(server));
			return CompletableFuture.failedFuture(ex);
		}
	}

	static int testCompression(CommandSourceStack source) {
		var server = source.getServer();
		PlatformHelper.CURRENT.pauseSaving(server);
		source.tell("Calculating...");

		Thread.startVirtualThread(() -> {
			var now = Instant.now();

			var methods = CompressionMethod.values();

			var totalTime = new Duration[methods.length];
			var totalSize = new long[methods.length];

			for (int i = 0; i < methods.length; ++i) {
				totalTime[i] = Duration.ZERO;
				totalSize[i] = 0L;
			}

			try {
				var from = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().toRealPath();

				try (var stream = Files.walk(from)) {
					for (var file : stream.filter(Files::isRegularFile).toList()) {
						VidLib.LOGGER.info("### " + from.relativize(file));

						now = Instant.now();
						var raw = Files.readAllBytes(file);
						var rawTime = Duration.between(now, Instant.now());

						totalTime[CompressionMethod.NONE.ordinal()] = totalTime[CompressionMethod.NONE.ordinal()].plus(rawTime);
						totalSize[CompressionMethod.NONE.ordinal()] += raw.length;
						VidLib.LOGGER.info("# none");
						VidLib.LOGGER.info("- Time: " + StringUtils.timer(rawTime.toMillis()));
						VidLib.LOGGER.info("- Size: " + StringUtils.siByteSize(raw.length));

						for (var method : methods) {
							if (method == CompressionMethod.NONE) {
								continue;
							}

							now = Instant.now();
							var bytes = method.compress(raw);
							var time = Duration.between(now, Instant.now());

							totalTime[method.ordinal()] = totalTime[method.ordinal()].plus(time);
							totalSize[method.ordinal()] += bytes.length;

							VidLib.LOGGER.info("# " + method.name);
							VidLib.LOGGER.info("- Time: " + StringUtils.timer(time.toMillis()));
							VidLib.LOGGER.info("- Size: " + StringUtils.siByteSize(bytes.length));
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			server.execute(() -> {
				PlatformHelper.CURRENT.resumeSaving(server);
				source.tell("Done! Results:");

				for (var method : methods) {
					source.tell("# " + method.name);
					var time = totalTime[method.ordinal()].toMillis();
					var size = totalSize[method.ordinal()];

					source.tell("- Time: " + StringUtils.timer(time));
					source.tell("- Size: " + StringUtils.siByteSize(size));
				}
			});
		});

		return 1;
	}

	static int backup(CommandSourceStack source, String customName) {
		var now = Instant.now();
		source.broadcast("Creating a world backup...");
		backup(source.getServer(), source.getServer().getWorldPath(LevelResource.ROOT), now, customName).thenAccept(name -> source.sendSuccess(() -> Component.literal("Saved a backup of '%s' (%.01f s)".formatted(name, Duration.between(now, Instant.now()).toMillis() / 1000F)), true));
		return 1;
	}
}
