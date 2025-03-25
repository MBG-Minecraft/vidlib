package dev.beast.mods.shimmer.feature.vote;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class VoteCommands {
	@AutoRegister
	public static final ServerCommandHolder COMMAND = new ServerCommandHolder("voting-screen", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.argument("data", CompoundTagArgument.compoundTag())
				.then(Commands.argument("title", ComponentArgument.textComponent(buildContext))
					.then(Commands.argument("subtitle", ComponentArgument.textComponent(buildContext))
						.then(Commands.argument("yes-label", ComponentArgument.textComponent(buildContext))
							.then(Commands.argument("no-label", ComponentArgument.textComponent(buildContext))
								.executes(ctx -> openScreen(
									EntityArgument.getPlayers(ctx, "player"),
									CompoundTagArgument.getCompoundTag(ctx, "data"),
									ComponentArgument.getComponent(ctx, "title"),
									ComponentArgument.getComponent(ctx, "subtitle"),
									ComponentArgument.getComponent(ctx, "yes-label"),
									ComponentArgument.getComponent(ctx, "no-label")
								))
							)
						)
						.executes(ctx -> openScreen(
							EntityArgument.getPlayers(ctx, "player"),
							CompoundTagArgument.getCompoundTag(ctx, "data"),
							ComponentArgument.getComponent(ctx, "title"),
							ComponentArgument.getComponent(ctx, "subtitle"),
							CommonComponents.GUI_YES,
							CommonComponents.GUI_NO
						))
					)
					.executes(ctx -> openScreen(
						EntityArgument.getPlayers(ctx, "player"),
						CompoundTagArgument.getCompoundTag(ctx, "data"),
						ComponentArgument.getComponent(ctx, "title"),
						Component.empty(),
						CommonComponents.GUI_YES,
						CommonComponents.GUI_NO
					))
				)
			)
		)
	);

	public static int openScreen(Collection<ServerPlayer> players, CompoundTag data, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		for (var player : players) {
			player.openVoteScreen(data, title, subtitle, yesLabel, noLabel);
		}

		return 1;
	}
}
