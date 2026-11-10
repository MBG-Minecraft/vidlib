package dev.latvian.mods.vidlib.feature.actor;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;

import java.util.Locale;

public interface ActorCommands {
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (ctx, builder) -> {
		var input = builder.getRemaining().toLowerCase(Locale.ROOT);

		for (var id : ActorRecording.getReplay(Minecraft.getInstance().level.registryAccess()).keySet()) {
			if (SharedSuggestionProvider.matchesSubStr(input, id)) {
				builder.suggest(id);
			}
		}

		return builder.buildFuture();
	};

	@ClientAutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("actor", (command, buildContext) -> {
		command.then(Commands.literal("record")
			.then(Commands.argument("username", StringArgumentType.word())
				.executes(ctx -> {
					var mc = Minecraft.getInstance();
					var session = ClientSessionData.of(mc);
					var profile = new NameAndId(mc.services().profileResolver().fetchByName(StringArgumentType.getString(ctx, "username")).orElseThrow());

					mc.player.sendOverlayMessage(Component.literal("3..."));
					session.schedule(20, () -> mc.player.sendOverlayMessage(Component.literal("2...")));
					session.schedule(40, () -> mc.player.sendOverlayMessage(Component.literal("1...")));
					session.schedule(60, () -> {
						mc.player.sendOverlayMessage(Component.literal("Go!"));
						session.startActorRecording(profile);
					});

					return 1;
				})
			)
		);

		command.then(Commands.literal("stop-recording")
			.executes(ctx -> {
				var mc = Minecraft.getInstance();
				var session = ClientSessionData.of(mc);
				session.stopActorRecording();
				return 1;
			})
		);

		command.then(Commands.literal("replay")
			.then(Commands.argument("recording", StringArgumentType.string())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> {
					var mc = Minecraft.getInstance();
					var session = ClientSessionData.of(mc);
					session.replayActorRecording(StringArgumentType.getString(ctx, "recording"));
					return 1;
				})
			)
			.executes(ctx -> {
				var mc = Minecraft.getInstance();
				var session = ClientSessionData.of(mc);
				session.replayActorRecording();
				return 1;
			})
		);
	});
}
