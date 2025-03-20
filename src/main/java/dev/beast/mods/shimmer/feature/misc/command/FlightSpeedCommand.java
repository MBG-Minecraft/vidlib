package dev.beast.mods.shimmer.feature.misc.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public interface FlightSpeedCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("flight-speed", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.argument("modifier", FloatArgumentType.floatArg(0F, 100F))
			.executes(ctx -> flightSpeed(ctx.getSource().getPlayerOrException(), FloatArgumentType.getFloat(ctx, "modifier")))
		)
	);

	private static int flightSpeed(ServerPlayer player, float modifier) {
		player.setFlightSpeedMod(modifier);
		return 1;
	}
}
