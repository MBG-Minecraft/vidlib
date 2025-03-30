package dev.beast.mods.shimmer.feature.misc.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public interface FlightSpeedCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("flight-speed", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("modifier", FloatArgumentType.floatArg(0F, 100F))
			.executes(ctx -> flightSpeed(List.of(ctx.getSource().getPlayerOrException()), FloatArgumentType.getFloat(ctx, "modifier")))
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> flightSpeed(EntityArgument.getPlayers(ctx, "player"), FloatArgumentType.getFloat(ctx, "modifier")))
			)
		)
	);

	private static int flightSpeed(Collection<ServerPlayer> players, float modifier) {
		for (var player : players) {
			player.setFlightSpeedMod(modifier);
		}

		return 1;
	}
}
