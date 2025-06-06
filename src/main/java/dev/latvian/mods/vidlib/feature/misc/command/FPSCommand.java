package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.server.level.ServerPlayer;

public interface FPSCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("fps", (command, buildContext) -> command
		.executes(ctx -> fps(ctx.getSource().getPlayerOrException()))
	);

	private static int fps(ServerPlayer player) {
		player.setShowFPS(!player.getShowFPS());
		return 1;
	}
}
