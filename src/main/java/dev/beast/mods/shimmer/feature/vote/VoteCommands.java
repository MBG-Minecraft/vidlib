package dev.beast.mods.shimmer.feature.vote;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import it.unimi.dsi.fastutil.ints.IntList;
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
	public static final ServerCommandHolder YES_NO_COMMAND = new ServerCommandHolder("voting-screen", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.literal("yes-no")
				.then(Commands.argument("extra-data", CompoundTagArgument.compoundTag())
					.then(Commands.argument("title", ComponentArgument.textComponent(buildContext))
						.then(Commands.argument("subtitle", ComponentArgument.textComponent(buildContext))
							.then(Commands.argument("yes-label", ComponentArgument.textComponent(buildContext))
								.then(Commands.argument("no-label", ComponentArgument.textComponent(buildContext))
									.executes(ctx -> yesNoScreen(
										EntityArgument.getPlayers(ctx, "player"),
										CompoundTagArgument.getCompoundTag(ctx, "extra-data"),
										ComponentArgument.getResolvedComponent(ctx, "title"),
										ComponentArgument.getResolvedComponent(ctx, "subtitle"),
										ComponentArgument.getResolvedComponent(ctx, "yes-label"),
										ComponentArgument.getResolvedComponent(ctx, "no-label")
									))
								)
							)
							.executes(ctx -> yesNoScreen(
								EntityArgument.getPlayers(ctx, "player"),
								CompoundTagArgument.getCompoundTag(ctx, "extra-data"),
								ComponentArgument.getResolvedComponent(ctx, "title"),
								ComponentArgument.getResolvedComponent(ctx, "subtitle"),
								CommonComponents.GUI_YES,
								CommonComponents.GUI_NO
							))
						)
						.executes(ctx -> yesNoScreen(
							EntityArgument.getPlayers(ctx, "player"),
							CompoundTagArgument.getCompoundTag(ctx, "extra-data"),
							ComponentArgument.getResolvedComponent(ctx, "title"),
							Component.empty(),
							CommonComponents.GUI_YES,
							CommonComponents.GUI_NO
						))
					)
				)
			)
			.then(Commands.literal("number")
				.then(Commands.argument("extra-data", CompoundTagArgument.compoundTag())
					.then(Commands.argument("title", ComponentArgument.textComponent(buildContext))
						.then(Commands.argument("subtitle", ComponentArgument.textComponent(buildContext))
							.executes(ctx -> numberScreen(
								EntityArgument.getPlayers(ctx, "player"),
								CompoundTagArgument.getCompoundTag(ctx, "extra-data"),
								ComponentArgument.getResolvedComponent(ctx, "title"),
								ComponentArgument.getResolvedComponent(ctx, "subtitle")
							))
						)
						.executes(ctx -> numberScreen(
							EntityArgument.getPlayers(ctx, "player"),
							CompoundTagArgument.getCompoundTag(ctx, "extra-data"),
							ComponentArgument.getResolvedComponent(ctx, "title"),
							Component.empty()
						))
					)
				)
			)
		)
	);

	public static int yesNoScreen(Collection<ServerPlayer> players, CompoundTag data, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		for (var player : players) {
			player.openYesNoVotingScreen(data, title, subtitle, yesLabel, noLabel);
		}

		return 1;
	}

	public static int numberScreen(Collection<ServerPlayer> players, CompoundTag data, Component title, Component subtitle) {
		for (var player : players) {
			player.openNumberVotingScreen(data, title, subtitle, 10, IntList.of(1, 7, 9));
		}

		return 1;
	}
}
