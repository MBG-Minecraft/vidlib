package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;

public interface PlayerDataCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("player-data", (command, buildContext) -> {
		command.requires(source -> source.hasPermission(2));
		var nbtOps = buildContext.createSerializationContext(NbtOps.INSTANCE);

		var playerCmd = Commands.argument("player", GameProfileArgument.gameProfile());

		for (var key : DataKey.PLAYER.all.values()) {
			var cmd = Commands.literal(key.id());

			cmd.then(Commands.literal("get")
				.executes(ctx -> {
					for (var player : GameProfileArgument.getGameProfiles(ctx, "player")) {
						var playerData = ctx.getSource().getServer().vl$getOrLoadServerSession(player.getId());

						ctx.getSource().sendSuccess(() -> {
							var value = playerData.dataMap.get(key);
							var nbt = key.type().codec().encodeStart(nbtOps, Cast.to(value)).getOrThrow();
							return Component.literal(player.getName() + ": ").append(NbtUtils.toPrettyComponent(nbt));
						}, false);
					}

					return 1;
				})
			);

			cmd.then(Commands.literal("set")
				.then(Commands.argument("value", key.command().argument(buildContext))
					.executes(ctx -> {
						var value = key.command().get(ctx, "value");

						for (var player : GameProfileArgument.getGameProfiles(ctx, "player")) {
							var playerData = ctx.getSource().getServer().vl$getOrLoadServerSession(player.getId());
							playerData.dataMap.set(key, Cast.to(value));
						}

						return 1;
					})
				)
			);

			cmd.then(Commands.literal("reset")
				.executes(ctx -> {
					for (var player : GameProfileArgument.getGameProfiles(ctx, "player")) {
						var playerData = ctx.getSource().getServer().vl$getOrLoadServerSession(player.getId());
						playerData.dataMap.set(key, Cast.to(key.defaultValue()));
					}

					return 1;
				})
			);

			playerCmd.then(cmd);
		}

		command.then(playerCmd);
	});
}
