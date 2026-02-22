package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public interface BackupCommand {
	// @AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("backup", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.executes(ctx -> backup(ctx.getSource()))
	);

	static String backup(MinecraftServer server) {
		for (var level : server.getAllLevels()) {
			if (level != null) {
				level.noSave = true;
			}
		}

		var name = CommonGameEngine.INSTANCE.getBackupInfo(server);
		var from = server.getWorldPath(LevelResource.ROOT);
		var fromName = from.getFileName().toString();
		var to = from.resolveSibling(fromName + "-" + name);
		var toName = to.getFileName().toString();

		server.saveEverything(true, true, true);

		/*
		if (Files.exists()) {

		}

		try {
			try {
				new ProcessBuilder("cp", "-R", fromName, toName)
					.directory(from.getParent().toFile())
					.start()
					.waitFor();
			} catch (Exception ex) {
				ex.printStackTrace();
				IOUtils.copyRecursively(from, to);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		 */

		for (var level : server.getAllLevels()) {
			if (level != null) {
				level.noSave = false;
			}
		}

		return toName;
	}

	static int backup(CommandSourceStack source) {
		var now = System.currentTimeMillis();
		var name = backup(source.getServer());
		source.sendSuccess(() -> Component.literal("Saved a backup of '%s' (%.01f s)".formatted(name, (System.currentTimeMillis() - now) / 1000F)), true);
		return 1;
	}
}
