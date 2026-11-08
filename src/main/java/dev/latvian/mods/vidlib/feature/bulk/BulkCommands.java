package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public class BulkCommands {
	@AutoRegister
	public static final ServerCommandHolder COMMAND = new ServerCommandHolder("bulk", (command, buildContext) -> command
		.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
		.then(Commands.literal("undo")
			.then(Commands.literal("last")
				.executes(ctx -> {
					var undo = LevelSessionData.of(ctx.getSource().getLevel()).undoLastModification(ctx.getSource().getLevel());
					ctx.getSource().sendSuccess(() -> Component.literal("Restored %,d blocks".formatted(undo)), true);
					return undo;
				})
			)
			.then(Commands.literal("all")
				.executes(ctx -> {
					var undo = LevelSessionData.of(ctx.getSource().getLevel()).undoAllModifications(ctx.getSource().getLevel());
					ctx.getSource().sendSuccess(() -> Component.literal("Restored %,d blocks".formatted(undo)), true);
					return undo;
				})
			)
			.then(Commands.literal("forget")
				.executes(ctx -> {
					LevelSessionData.of(ctx.getSource().getLevel()).undoable.clear();
					ctx.getSource().sendSuccess(() -> Component.literal("Forgot all modifications"), true);
					return 1;
				})
			)
		)
	);
}
