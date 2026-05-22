package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public interface SuperOverrideCommand {
	@ClientAutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("super-override", (command, buildContext) -> {
		command.requires(source -> source.getSidedLevel().isClientSide);

		{
			var serverDataCommand = Commands.literal("server-data");

			var set = Commands.literal("set");
			var reset = Commands.literal("reset");

			for (var key : DataKey.SERVER.all.values()) {
				set.then(Commands.literal(key.id())
					.then(Commands.argument("value", key.command().argument(buildContext))
						.executes(ctx -> {
							var session = Minecraft.getInstance().player.vl$sessionData();
							var value = key.command().get(ctx, "value");
							session.serverDataMap.setSuperOverride(key, value);
							return 1;
						})
					)
				);

				reset.then(Commands.literal(key.id())
					.executes(ctx -> {
						var session = Minecraft.getInstance().player.vl$sessionData();
						session.serverDataMap.removeSuperOverride(key);
						return 1;
					})
				);
			}

			serverDataCommand.then(set);
			serverDataCommand.then(reset);
			command.then(serverDataCommand);
		}

		{
			var playerDataCommand = Commands.literal("player-data");

			var set = Commands.argument("player", EntityArgument.players());
			var reset = Commands.argument("player", EntityArgument.players());

			for (var key : DataKey.PLAYER.all.values()) {
				set.then(Commands.literal(key.id())
					.then(Commands.argument("value", key.command().argument(buildContext))
						.executes(ctx -> {
							var session = Minecraft.getInstance().player.vl$sessionData();
							var value = key.command().get(ctx, "value");

							for (var player : Minecraft.getInstance().level.selectPlayers(ctx, "player")) {
								var psession = session.getClientSessionData(player.getUUID());
								psession.dataMap.setSuperOverride(key, value);
							}

							return 1;
						})
					)
				);

				reset.then(Commands.literal(key.id())
					.executes(ctx -> {
						var session = Minecraft.getInstance().player.vl$sessionData();

						for (var player : Minecraft.getInstance().level.selectPlayers(ctx, "player")) {
							var psession = session.getClientSessionData(player.getUUID());
							psession.dataMap.removeSuperOverride(key);
						}

						return 1;
					})
				);
			}

			playerDataCommand.then(Commands.literal("set").then(set));
			playerDataCommand.then(Commands.literal("reset").then(reset));
			command.then(playerDataCommand);
		}

		command.then(Commands.literal("time")
			.executes(ctx -> {
				var time = Long.toUnsignedString(Minecraft.getInstance().level.getGameTime());
				ctx.getSource().sendSuccess(() -> Component.literal("Time: " + time).withStyle(Style.EMPTY.withClickToCopyToClipboard(time)), false);
				return 1;
			})
		);
	});
}
