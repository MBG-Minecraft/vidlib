package dev.latvian.mods.vidlib.feature.fade;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public interface FadeCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("fade", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.argument("color", RegisteredDataType.COLOR.argument(buildContext))
				.then(Commands.argument("fade-in", IntegerArgumentType.integer(0))
					.then(Commands.argument("pause", IntegerArgumentType.integer(0))
						.executes(ctx -> {
							var fade = new Fade(RegisteredDataType.COLOR.get(ctx, "color"), IntegerArgumentType.getInteger(ctx, "fade-in"), IntegerArgumentType.getInteger(ctx, "pause"));

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
