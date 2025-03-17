package dev.beast.mods.shimmer.feature.misc.command;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public interface HealCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("heal", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.executes(ctx -> heal(EntityArgument.getPlayers(ctx, "player")))
		)
		.executes(ctx -> heal(List.of(ctx.getSource().getPlayerOrException())))
	);

	private static int heal(Collection<ServerPlayer> players) {
		for (var player : players) {
			player.heal();
		}

		return 1;
	}
}
