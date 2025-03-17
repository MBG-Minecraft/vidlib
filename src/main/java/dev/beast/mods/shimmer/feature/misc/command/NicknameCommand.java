package dev.beast.mods.shimmer.feature.misc.command;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.misc.RefreshNamePayload;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public interface NicknameCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("nickname", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.argument("nickname", ComponentArgument.textComponent(buildContext))
				.executes(ctx -> nickname(EntityArgument.getOptionalPlayers(ctx, "player"), ComponentArgument.getComponent(ctx, "nickname")))
			)
		)
		.then(Commands.argument("nickname", ComponentArgument.textComponent(buildContext))
			.executes(ctx -> nickname(List.of(ctx.getSource().getPlayerOrException()), ComponentArgument.getComponent(ctx, "nickname")))
		)
	);

	private static int nickname(Collection<ServerPlayer> players, Component name) {
		for (var player : players) {
			player.set(InternalPlayerData.NICKNAME, name);
			player.refreshDisplayName();
			player.refreshTabListName();
			player.level().s2c(new RefreshNamePayload(player.getUUID()));
		}

		return 1;
	}
}
