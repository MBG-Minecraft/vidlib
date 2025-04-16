package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public interface SuspensionCommands {
	@AutoRegister
	ServerCommandHolder SUSPEND_COMMAND = new ServerCommandHolder("suspend", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.executes(ctx -> suspend(EntityArgument.getPlayers(ctx, "player"), true))
		)
	);

	@AutoRegister
	ServerCommandHolder UNSUSPEND_COMMAND = new ServerCommandHolder("unsuspend", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.executes(ctx -> suspend(EntityArgument.getPlayers(ctx, "player"), false))
		)
	);

	private static int suspend(Collection<ServerPlayer> players, boolean suspend) {
		for (var player : players) {
			player.setSuspended(suspend);
		}

		return 1;
	}
}
