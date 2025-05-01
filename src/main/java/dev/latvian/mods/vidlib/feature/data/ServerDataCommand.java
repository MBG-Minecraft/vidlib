package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.util.Cast;
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

		for (var data : DataType.SERVER.all.values()) {
			get.then(Commands.literal(data.id())
				.executes(ctx -> {
					ctx.getSource().sendSuccess(() -> {
						var value = ctx.getSource().getServer().getServerData().get(data);
						var nbt = data.type().codec().encodeStart(nbtOps, Cast.to(value)).getOrThrow();
						return Component.literal("Server: ").append(NbtUtils.toPrettyComponent(nbt));
					}, false);
					return 1;
				})
			);

			set.then(Commands.literal(data.id())
				.then(Commands.argument("value", data.type().argument(buildContext))
					.executes(ctx -> {
						var value = data.type().get(ctx, "value");
						ctx.getSource().getServer().getServerData().set(data, Cast.to(value));
						return 1;
					})
				)
			);

			reset.then(Commands.literal(data.id())
				.executes(ctx -> {
					ctx.getSource().getServer().getServerData().set(data, Cast.to(data.defaultValue()));
					return 1;
				})
			);
		}

		command.then(get);
		command.then(set);
		command.then(reset);
	});
}
