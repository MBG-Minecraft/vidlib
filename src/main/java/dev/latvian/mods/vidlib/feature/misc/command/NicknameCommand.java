package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public interface NicknameCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("nickname", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.argument("nickname", ComponentArgument.textComponent(buildContext))
				.executes(ctx -> nickname(EntityArgument.getOptionalPlayers(ctx, "player"), ComponentArgument.getResolvedComponent(ctx, "nickname")))
			)
		)
	);

	private static int nickname(Collection<ServerPlayer> players, Component name) {
		for (var player : players) {
			player.setNickname(name.copy());
		}

		return 1;
	}
}
