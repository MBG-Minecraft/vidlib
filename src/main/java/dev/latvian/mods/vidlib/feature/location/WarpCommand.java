package dev.latvian.mods.vidlib.feature.location;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public interface WarpCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("warp", (command, buildContext) -> command
		.then(Commands.argument("warp", Location.KNOWN_CODEC.argument(buildContext))
			.requires(source -> source.hasPermission(2))
			.executes(ctx -> warp(ctx.getSource().getPlayerOrException(), Location.KNOWN_CODEC.get(ctx, "warp")))
		)
	);

	private static int warp(ServerPlayer player, Location location) {
		player.teleport(location);
		return 1;
	}
}
