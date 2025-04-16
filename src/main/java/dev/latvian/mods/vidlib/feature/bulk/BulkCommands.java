package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class BulkCommands {
	@AutoRegister
	public static final ServerCommandHolder COMMAND = new ServerCommandHolder("bulk", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("undo")
			.then(Commands.literal("last")
				.executes(ctx -> {
					var undo = ctx.getSource().getLevel().undoLastModification();
					ctx.getSource().sendSuccess(() -> Component.literal("Restored %,d blocks".formatted(undo)), true);
					return undo;
				})
			)
			.then(Commands.literal("all")
				.executes(ctx -> {
					var undo = ctx.getSource().getLevel().undoAllModifications();
					ctx.getSource().sendSuccess(() -> Component.literal("Restored %,d blocks".formatted(undo)), true);
					return undo;
				})
			)
			.then(Commands.literal("forget")
				.executes(ctx -> {
					ctx.getSource().getLevel().vl$getUndoableModifications().clear();
					ctx.getSource().sendSuccess(() -> Component.literal("Forgot all modifications"), true);
					return 1;
				})
			)
		)
	);
}
