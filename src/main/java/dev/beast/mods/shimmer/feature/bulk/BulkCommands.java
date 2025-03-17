package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;

public class BulkCommands {
	@AutoRegister
	public static final ServerCommandHolder COMMAND = new ServerCommandHolder("bulk", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("undo")
			.then(Commands.literal("all")
				.executes(ctx -> {
					var level = ctx.getSource().getLevel();
					var builder = new OptimizedModificationBuilder();
					var undoable = ctx.getSource().getLevel().shimmer$getUndoableModifications();

					for (int i = undoable.size() - 1; i >= 0; i--) {
						undoable.get(i).undo(level, builder);
					}

					undoable.clear();
					ctx.getSource().getEntity().status("Restored %,d blocks".formatted(level.bulkModify(builder.build())));
					return 1;
				})
			)
			.executes(ctx -> {
				var undoable = ctx.getSource().getLevel().shimmer$getUndoableModifications();

				if (!undoable.isEmpty()) {
					var level = ctx.getSource().getLevel();
					var builder = new OptimizedModificationBuilder();
					undoable.getLast().undo(level, builder);
					undoable.removeLast();
					ctx.getSource().getEntity().status("Restored %,d blocks".formatted(level.bulkModify(builder.build())));
				}

				return 1;
			})
		)
	);
}
