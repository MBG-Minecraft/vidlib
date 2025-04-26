package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ClientCommandHolder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public interface SuperOverrideCommand {
	@AutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("super-override", (command, buildContext) -> {
		{
			var serverDataCommand = Commands.literal("server-data");

			var set = Commands.literal("set");
			var reset = Commands.literal("reset");

			for (var data : DataType.SERVER.all.values()) {
				set.then(Commands.literal(data.id().toString())
					.then(Commands.argument("value", data.type().argument(buildContext))
						.executes(ctx -> {
							var session = Minecraft.getInstance().player.shimmer$sessionData();
							var value = data.type().get(ctx, "value");

							if (session.serverDataMap.superOverrides == null) {
								session.serverDataMap.superOverrides = new Reference2ObjectArrayMap<>();
							}

							session.serverDataMap.superOverrides.put(data, value);
							return 1;
						})
					)
				);

				reset.then(Commands.literal(data.id().toString())
					.executes(ctx -> {
						var session = Minecraft.getInstance().player.shimmer$sessionData();

						if (session.serverDataMap.superOverrides != null) {
							session.serverDataMap.superOverrides.remove(data);

							if (session.serverDataMap.superOverrides.isEmpty()) {
								session.serverDataMap.superOverrides = null;
							}
						}

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

			for (var data : DataType.PLAYER.all.values()) {
				set.then(Commands.literal(data.id().toString())
					.then(Commands.argument("value", data.type().argument(buildContext))
						.executes(ctx -> {
							var session = Minecraft.getInstance().player.shimmer$sessionData();
							var value = data.type().get(ctx, "value");

							for (var player : Minecraft.getInstance().level.selectPlayers(ctx, "player")) {
								var psession = session.getClientSessionData(player.getUUID());

								if (psession.dataMap.superOverrides == null) {
									psession.dataMap.superOverrides = new Reference2ObjectArrayMap<>();
								}

								psession.dataMap.superOverrides.put(data, value);
							}

							return 1;
						})
					)
				);

				reset.then(Commands.literal(data.id().toString())
					.executes(ctx -> {
						var session = Minecraft.getInstance().player.shimmer$sessionData();

						for (var player : Minecraft.getInstance().level.selectPlayers(ctx, "player")) {
							var psession = session.getClientSessionData(player.getUUID());

							if (psession.dataMap.superOverrides != null) {
								psession.dataMap.superOverrides.remove(data);

								if (psession.dataMap.superOverrides.isEmpty()) {
									psession.dataMap.superOverrides = null;
								}
							}
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
