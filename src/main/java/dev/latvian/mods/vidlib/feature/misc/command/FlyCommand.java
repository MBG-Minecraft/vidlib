package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public interface FlyCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("fly", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("target", EntityArgument.player())
			.executes(ctx -> fly(EntityArgument.getPlayer(ctx, "target")))
		)
		.executes(ctx -> fly(ctx.getSource().getPlayerOrException()))
	);

	private static int fly(ServerPlayer player) {
		player.set(InternalPlayerData.CAN_FLY, !player.get(InternalPlayerData.CAN_FLY));
		return 1;
	}
}
