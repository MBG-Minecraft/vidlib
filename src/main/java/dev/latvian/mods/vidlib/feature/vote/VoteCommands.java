package dev.latvian.mods.vidlib.feature.vote;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collection;

public class VoteCommands {
	@AutoRegister
	public static final ServerCommandHolder YES_NO_COMMAND = new ServerCommandHolder("vote", (command, buildContext) -> command
		.then(Commands.argument("yes", IntegerArgumentType.integer(1, 10))
			.then(Commands.argument("extra-data", CompoundTagArgument.compoundTag())
				.executes(ctx -> vote(ctx.getSource(), 1, CompoundTagArgument.getCompoundTag(ctx, "extra-data")))
			)
			.executes(ctx -> vote(ctx.getSource(), 1, Empty.COMPOUND_TAG))
		)
		.then(Commands.argument("no", IntegerArgumentType.integer(1, 10))
			.then(Commands.argument("extra-data", CompoundTagArgument.compoundTag())
				.executes(ctx -> vote(ctx.getSource(), 0, CompoundTagArgument.getCompoundTag(ctx, "extra-data")))
			)
			.executes(ctx -> vote(ctx.getSource(), 0, Empty.COMPOUND_TAG))
		)
		.then(Commands.literal("for-option")
			.then(Commands.argument("option", IntegerArgumentType.integer(1, 10))
				.then(Commands.argument("extra-data", CompoundTagArgument.compoundTag())
					.executes(ctx -> vote(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "option") - 1, CompoundTagArgument.getCompoundTag(ctx, "extra-data")))
				)
				.executes(ctx -> vote(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "option") - 1, Empty.COMPOUND_TAG))
			)
		)
		.then(Commands.literal("open-screen")
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
		)
	);

	public static int vote(CommandSourceStack source, int option, CompoundTag extraData) throws CommandSyntaxException {
		var self = source.getPlayerOrException();

		if (NeoForge.EVENT_BUS.post(new PlayerVotedEvent(self, extraData, option)).isCanceled()) {
			self.vl$closeScreen();
			return 1;
		}

		return 0;
	}

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
