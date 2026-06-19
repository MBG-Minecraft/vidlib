package dev.latvian.mods.vidlib.feature.location;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.commands.Commands;

public interface WarpCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("warp", (command, buildContext) -> {
		command.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source));

		var locations = CommonGameEngine.INSTANCE.getWarpLocations();

		for (var location : locations) {
			command.then(Commands.literal(location.id())
				.requires(source -> !location.admin() || net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
				.executes(ctx -> {
					ctx.getSource().getPlayerOrException().teleport(location.pos().apply(ctx.getSource().getServer()));
					return 1;
				})
			);
		}
	});
}
