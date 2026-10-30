package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.permissions.Permissions;

public interface CutsceneCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("cutscene", (command, buildContext) -> command
		.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
		.then(Commands.literal("play")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("cutscene", Cutscene.COMMAND.argument(buildContext))
					.executes(ctx -> {
						var cutscene = Cutscene.COMMAND.get(ctx, "cutscene");

						for (var player : EntityArgument.getPlayers(ctx, "player")) {
							player.playCutscene(cutscene, KNumberVariables.EMPTY);
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
