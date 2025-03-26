package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public interface CutsceneCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("cutscene", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("play")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("id", Cutscene.KNOWN_CODEC.argument(buildContext))
					.suggests(Cutscene.REGISTRY.suggestionProvider)
					.executes(ctx -> {
						var cutscene = Cutscene.KNOWN_CODEC.get(ctx, "id");

						for (var player : EntityArgument.getPlayers(ctx, "player")) {
							player.playCutscene(cutscene, WorldNumberVariables.EMPTY);
						}

						return 1;
					})
				)
			)
		)
		.then(Commands.literal("create")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("data", Cutscene.DIRECT_KNOWN_CODEC.argument(buildContext))
					.executes(ctx -> {
						var cutscene = Cutscene.DIRECT_KNOWN_CODEC.get(ctx, "data");

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
