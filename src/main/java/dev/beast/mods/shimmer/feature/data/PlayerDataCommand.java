package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;

public interface PlayerDataCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("player-data", (command, buildContext) -> {
		command.requires(source -> source.hasPermission(2));
		var nbtOps = buildContext.createSerializationContext(NbtOps.INSTANCE);

		var get = Commands.argument("player", EntityArgument.players());
		var set = Commands.argument("player", EntityArgument.players());
		var reset = Commands.argument("player", EntityArgument.players());

		for (var data : DataType.PLAYER.all.values()) {
			get.then(Commands.literal(data.id().toString())
				.executes(ctx -> {
					for (var player : EntityArgument.getPlayers(ctx, "player")) {
						ctx.getSource().sendSuccess(() -> {
							var value = player.get(data);
							var nbt = data.type().codec().encodeStart(nbtOps, Cast.to(value)).getOrThrow();
							return Component.literal(player.getScoreboardName() + ": ").append(NbtUtils.toPrettyComponent(nbt));
						}, false);
					}

					return 1;
				})
			);

			set.then(Commands.literal(data.id().toString())
				.then(Commands.argument("value", data.type().argument(buildContext))
					.executes(ctx -> {
						var value = data.type().get(ctx, "value");

						for (var player : EntityArgument.getPlayers(ctx, "player")) {
							player.set(data, Cast.to(value));
						}

						return 1;
					})
				)
			);

			reset.then(Commands.literal(data.id().toString())
				.executes(ctx -> {
					for (var player : EntityArgument.getPlayers(ctx, "player")) {
						player.set(data, Cast.to(data.defaultValue()));
					}

					return 1;
				})
			);
		}

		command.then(Commands.literal("get").then(get));
		command.then(Commands.literal("set").then(set));
		command.then(Commands.literal("reset").then(reset));
	});
}
