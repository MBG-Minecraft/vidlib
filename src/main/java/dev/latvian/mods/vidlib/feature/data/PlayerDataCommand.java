package dev.latvian.mods.vidlib.feature.data;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public interface PlayerDataCommand {
	SuggestionProvider<CommandSourceStack> SESSION_NAMES = (ctx, builder) -> SharedSuggestionProvider.suggest(LevelSessionData.of(PlatformHelper.INSTANCE.getLevel(ctx.getSource())).globalData.playerMap.values().stream().map(PlayerSessionData::getName), builder);

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("player-data", (command, buildContext) -> {
		command.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));

		command.then(Commands.literal("preload-all")
			.executes(ctx -> {
				ServerSessionData.of(ctx.getSource().getServer()).preloadAllSessions();
				return 1;
			})
		);

		var nbtOps = buildContext.createSerializationContext(NbtOps.INSTANCE);

		var playerCmd = Commands.argument("player", GameProfileArgument.gameProfile());
		playerCmd.suggests(SESSION_NAMES);

		playerCmd.then(Commands.literal("erase")
			.executes(ctx -> {
				var vl = ServerSessionData.of(ctx.getSource().getServer());

				for (var player : GameProfileArgument.getGameProfiles(ctx, "player")) {
					vl.eraseServerSession(player.id());
				}

				return 1;
			})
		);

		for (var key : DataKey.PLAYER.all.values()) {
			var cmd = Commands.literal(key.id);

			cmd.then(Commands.literal("get")
				.executes(ctx -> {
					var vl = ServerSessionData.of(ctx.getSource().getServer());

					for (var player : GameProfileArgument.getGameProfiles(ctx, "player")) {
						var playerData = vl.getPlayer(player.id());

						ctx.getSource().sendSuccess(() -> {
							var value = playerData.dataMap.get(key);
							var nbt = key.type.codec().encodeStart(nbtOps, Cast.to(value)).getOrThrow();
							return Component.literal(player.name() + ": ").append(NbtUtils.toPrettyComponent(nbt));
						}, false);
					}

					return 1;
				})
			);

			cmd.then(Commands.literal("set")
				.then(Commands.argument("value", key.type.argument(buildContext))
					.executes(ctx -> {
						var vl = ServerSessionData.of(ctx.getSource().getServer());
						var value = key.type.get(ctx, "value");

						for (var player : GameProfileArgument.getGameProfiles(ctx, "player")) {
							var playerData = vl.getPlayer(player.id());
							playerData.dataMap.set(key, Cast.to(value));
						}

						return 1;
					})
				)
			);

			cmd.then(Commands.literal("reset")
				.executes(ctx -> {
					var vl = ServerSessionData.of(ctx.getSource().getServer());

					for (var player : GameProfileArgument.getGameProfiles(ctx, "player")) {
						var playerData = vl.getPlayer(player.id());
						playerData.dataMap.reset(key);
					}

					return 1;
				})
			);

			playerCmd.then(cmd);
		}

		command.then(playerCmd);
	});
}
