package dev.beast.mods.shimmer.feature.fade;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.math.Color;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public interface FadeCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("fade", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.argument("color", Color.KNOWN_CODEC.argument(buildContext))
				.then(Commands.argument("fade-in", IntegerArgumentType.integer(0))
					.then(Commands.argument("pause", IntegerArgumentType.integer(0))
						.executes(ctx -> {
							var fade = new Fade(Color.KNOWN_CODEC.get(ctx, "color"), IntegerArgumentType.getInteger(ctx, "fade-in"), IntegerArgumentType.getInteger(ctx, "pause"));

							for (var player : EntityArgument.getPlayers(ctx, "player")) {
								player.setScreenFade(fade);
							}

							return 1;
						})
					)
				)
			)
		)
	);
}
