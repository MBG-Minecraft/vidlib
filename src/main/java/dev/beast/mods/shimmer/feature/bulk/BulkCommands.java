package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;

public class BulkCommands {
	@AutoRegister
	public static final ServerCommandHolder COMMAND = new ServerCommandHolder("bulk", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("undo")
			.then(Commands.literal("last")
				.executes(ctx -> {
					var undo = ctx.getSource().getLevel().undoLastModification();
					ctx.getSource().getEntity().status("Restored %,d blocks".formatted(undo));
					return undo;
				})
			)
			.then(Commands.literal("all")
				.executes(ctx -> {
					var undo = ctx.getSource().getLevel().undoAllModifications();
					ctx.getSource().getEntity().status("Restored %,d blocks".formatted(undo));
					return undo;
				})
			)
			.then(Commands.literal("forget")
				.executes(ctx -> {
					ctx.getSource().getLevel().shimmer$getUndoableModifications().clear();
					ctx.getSource().getEntity().status("Forgot all modifications");
					return 1;
				})
			)
		)
	);
}
