package dev.latvian.mods.vidlib.feature.misc.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

	static CompletableFuture<Path> backup(MinecraftServer server, Instant now, String customName) {
		PlatformHelper.CURRENT.pauseSaving(server);

		return CompletableFuture.supplyAsync(() -> {
			try {
				var name = CommonGameEngine.INSTANCE.getFullBackupInfo(server, now, customName);
				var from = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().toRealPath();
				var fromName = from.getFileName().toString();
				var to = from.resolveSibling(fromName + "-" + name);
				var toName = to.getFileName().toString();

				try {
					var process = new ProcessBuilder(IOUtils.platformCopy(fromName, toName))
						.directory(from.getParent().toAbsolutePath().toFile())
						.start();

					process.waitFor(1L, TimeUnit.MINUTES);
					VidLib.LOGGER.info("Backup status: " + process.exitValue());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				return to;
			} catch (Throwable ex) {
				ex.printStackTrace();
			} finally {
				server.execute(() -> PlatformHelper.CURRENT.resumeSaving(server));
			}

			throw new IllegalStateException("Failed to create a backup");
		});
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
		backup(source.getServer(), now, customName).thenAccept(name -> source.sendSuccess(() -> Component.literal("Saved a backup of '%s' (%.01f s)".formatted(name, Duration.between(now, Instant.now()).toMillis() / 1000F)), true));
		return 1;
	}
}
