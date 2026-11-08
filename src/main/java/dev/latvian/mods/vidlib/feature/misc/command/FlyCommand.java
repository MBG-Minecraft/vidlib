package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public interface FlyCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("fly", (command, buildContext) -> command
		.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
		.then(Commands.argument("target", EntityArgument.player())
			.executes(ctx -> fly(EntityArgument.getPlayer(ctx, "target")))
		)
		.executes(ctx -> fly(ctx.getSource().getPlayerOrException()))
	);

	private static int fly(ServerPlayer player) {
		var session = PlayerSessionData.of(player);
		session.setCanFly(!session.getCanFly());
		return 1;
	}
}
