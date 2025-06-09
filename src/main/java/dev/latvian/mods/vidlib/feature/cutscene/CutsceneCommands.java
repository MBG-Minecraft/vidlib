package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public interface CutsceneCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("cutscene", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("play")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("cutscene", Cutscene.COMMAND.argument(buildContext))
					.executes(ctx -> {
						var cutscene = Cutscene.COMMAND.get(ctx, "cutscene");

						for (var player : EntityArgument.getPlayers(ctx, "player")) {
							player.playCutscene(cutscene, WorldNumberVariables.EMPTY);
						}

						return 1;
					})
				)
			)
		)
		.then(Commands.literal("stop")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> {
					for (var player : EntityArgument.getPlayers(ctx, "player")) {
						player.stopCutscene();
					}

					return 1;
				})
			)
		)
	);
}
