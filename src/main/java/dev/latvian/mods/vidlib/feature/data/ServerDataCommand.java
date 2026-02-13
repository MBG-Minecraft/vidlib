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

		for (var key : DataKey.SERVER.all.values()) {
			var cmd = Commands.literal(key.id());

			cmd.then(Commands.literal("get")
				.executes(ctx -> {
					ctx.getSource().sendSuccess(() -> {
						var value = ctx.getSource().getServer().getDataMap().get(key);
						var nbt = key.type().codec().encodeStart(nbtOps, Cast.to(value)).getOrThrow();
						return Component.literal("Server: ").append(NbtUtils.toPrettyComponent(nbt));
					}, false);
					return 1;
				})
			);

			cmd.then(Commands.literal("set")
				.then(Commands.argument("value", key.command().argument(buildContext))
					.executes(ctx -> {
						var value = key.command().get(ctx, "value");
						ctx.getSource().getServer().getDataMap().set(key, Cast.to(value));
						return 1;
					})
				)
			);

			cmd.then(Commands.literal("reset").executes(ctx -> {
				ctx.getSource().getServer().getDataMap().reset(key);
				return 1;
			}));

			command.then(cmd);
		}
	});
}
