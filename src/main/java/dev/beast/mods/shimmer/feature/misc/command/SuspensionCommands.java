package dev.beast.mods.shimmer.feature.misc.command;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public interface SuspensionCommands {
	@AutoRegister
	ServerCommandHolder SUSPEND_COMMAND = new ServerCommandHolder("suspend", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.executes(ctx -> suspend(EntityArgument.getPlayers(ctx, "player"), true))
		)
	);

	@AutoRegister
	ServerCommandHolder UNSUSPEND_COMMAND = new ServerCommandHolder("unsuspend", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.executes(ctx -> suspend(EntityArgument.getPlayers(ctx, "player"), false))
		)
	);

	private static int suspend(Collection<ServerPlayer> players, boolean suspend) {
		for (var player : players) {
			player.set(InternalPlayerData.SUSPENDED, suspend);
		}

		return 1;
	}
}
