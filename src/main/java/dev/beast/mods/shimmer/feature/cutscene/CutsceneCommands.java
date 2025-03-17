package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;

public interface CutsceneCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("cutscene", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("play")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.executes(ctx -> {
					ctx.getSource().getPlayerOrException().playCutscene(ResourceLocationArgument.getId(ctx, "id"), WorldNumberVariables.EMPTY);
					return 1;
				})
			)
		)
		.then(Commands.literal("create")
			.then(Commands.argument("data", Cutscene.KNOWN_CODEC.argument(buildContext))
				.executes(ctx -> {
					ctx.getSource().getPlayerOrException().playCutscene(Cutscene.KNOWN_CODEC.get(ctx, "data"), WorldNumberVariables.EMPTY);
					return 1;
				})
			)
		)
		.then(Commands.literal("stop")
			.executes(ctx -> {
				ctx.getSource().getPlayerOrException().stopCutscene();
				return 1;
			})
		)
	);
}
