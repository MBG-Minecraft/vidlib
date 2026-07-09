package dev.latvian.mods.vidlib.feature.screeneffect.fade;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public interface FadeCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("fade", (command, buildContext) -> command
		.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.argument("color", Gradient.DATA_TYPE.argument(buildContext))
				.then(Commands.argument("fade-in", IntegerArgumentType.integer(0))
					.then(Commands.argument("pause", IntegerArgumentType.integer(0))
						.executes(ctx -> {
							var fade = new Fade(Gradient.DATA_TYPE.get(ctx, "color"), IntegerArgumentType.getInteger(ctx, "fade-in"), IntegerArgumentType.getInteger(ctx, "pause"));

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
