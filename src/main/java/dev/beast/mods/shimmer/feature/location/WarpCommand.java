package dev.beast.mods.shimmer.feature.location;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public interface WarpCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("warp", (command, buildContext) -> command
		.then(Commands.argument("warp", Location.KNOWN_CODEC.argument(buildContext))
			.requires(source -> source.hasPermission(2))
			.suggests(Location.REGISTRY.suggestionProvider)
			.executes(ctx -> warp(ctx.getSource().getPlayerOrException(), Location.KNOWN_CODEC.get(ctx, "warp")))
		)
	);

	private static int warp(ServerPlayer player, Location location) {
		player.teleport(location);
		return 1;
	}
}
