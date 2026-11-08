package dev.latvian.mods.vidlib.feature.misc.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Collection;
import java.util.List;

public interface SpeedCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("speed", (command, buildContext) -> command
		.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
		.then(Commands.argument("modifier", FloatArgumentType.floatArg(0F, 100F))
			.executes(ctx -> flightSpeed(List.of(ctx.getSource().getPlayerOrException()), FloatArgumentType.getFloat(ctx, "modifier")))
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> flightSpeed(EntityArgument.getPlayers(ctx, "player"), FloatArgumentType.getFloat(ctx, "modifier")))
			)
		)
	);

	private static int flightSpeed(Collection<ServerPlayer> players, float modifier) {
		for (var player : players) {
			PlayerSessionData.of(player).setFlightSpeedMod(modifier);
		}

		return 1;
	}
}
