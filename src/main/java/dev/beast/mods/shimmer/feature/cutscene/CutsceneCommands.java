package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.util.registry.VideoResourceLocationArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface CutsceneCommands {
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = VideoResourceLocationArgument.registerSuggestionProvider(Shimmer.id("cutscene"), () -> Cutscene.SERVER.getMap().keySet());

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("cutscene", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("play")
			.then(Commands.argument("id", VideoResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> {
					ctx.getSource().getPlayerOrException().playCutscene(VideoResourceLocationArgument.getId(ctx, "id"), WorldNumberVariables.EMPTY);
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
