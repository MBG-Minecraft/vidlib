package dev.beast.mods.shimmer.feature.misc.command;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public interface SyncCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("sync", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.executes(ctx -> sync(EntityArgument.getPlayers(ctx, "player")))
		)
		.executes(ctx -> sync(List.of(ctx.getSource().getPlayerOrException())))
	);

	private static int sync(Collection<ServerPlayer> players) {
		for (var player : players) {
			Shimmer.sync(player, false);
		}

		return 1;
	}
}
