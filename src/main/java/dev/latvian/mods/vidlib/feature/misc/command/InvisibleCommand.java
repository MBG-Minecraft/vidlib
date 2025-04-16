package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public interface InvisibleCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("invisible", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.executes(ctx -> invisible(EntityArgument.getPlayers(ctx, "player")))
		)
		.executes(ctx -> invisible(List.of(ctx.getSource().getPlayerOrException())))
	);

	private static int invisible(Collection<ServerPlayer> players) {
		for (var player : players) {
			player.heal();
		}

		return 1;
	}
}
