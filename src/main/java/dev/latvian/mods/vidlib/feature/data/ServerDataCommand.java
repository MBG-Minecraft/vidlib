package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;

public interface ServerDataCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("server-data", (command, buildContext) -> {
		command.requires(source -> source.hasPermission(2));
		var nbtOps = buildContext.createSerializationContext(NbtOps.INSTANCE);

		var get = Commands.literal("get");
		var set = Commands.literal("set");
		var reset = Commands.literal("reset");

		for (var key : DataKey.SERVER.all.values()) {
			get.then(Commands.literal(key.id())
				.executes(ctx -> {
					ctx.getSource().sendSuccess(() -> {
						var value = ctx.getSource().getServer().getServerData().get(key);
						var nbt = key.type().codec().encodeStart(nbtOps, Cast.to(value)).getOrThrow();
						return Component.literal("Server: ").append(NbtUtils.toPrettyComponent(nbt));
					}, false);
					return 1;
				})
			);

			set.then(Commands.literal(key.id())
				.then(Commands.argument("value", key.command().argument(buildContext))
					.executes(ctx -> {
						var value = key.command().get(ctx, "value");
						ctx.getSource().getServer().getServerData().set(key, Cast.to(value));
						return 1;
					})
				)
			);

			reset.then(Commands.literal(key.id())
				.executes(ctx -> {
					ctx.getSource().getServer().getServerData().set(key, Cast.to(key.defaultValue()));
					return 1;
				})
			);
		}

		command.then(get);
		command.then(set);
		command.then(reset);
	});
}
