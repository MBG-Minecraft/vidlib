package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.session.ServerWorldSessionData;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public interface ServerDataCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("server-data", (command, buildContext) -> {
		command.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
		var nbtOps = buildContext.createSerializationContext(NbtOps.INSTANCE);

		for (var key : DataKey.SERVER.all.values()) {
			var cmd = Commands.literal(key.id());

			cmd.then(Commands.literal("get")
				.executes(ctx -> {
					ctx.getSource().sendSuccess(() -> {
						var vl = ServerWorldSessionData.of(ctx.getSource().getServer());
						var value = vl.dataMap.get(key);
						var nbt = key.type().codec().encodeStart(nbtOps, Cast.to(value)).getOrThrow();
						return Component.literal("Server: ").append(NbtUtils.toPrettyComponent(nbt));
					}, false);
					return 1;
				})
			);

			cmd.then(Commands.literal("set")
				.then(Commands.argument("value", key.type().argument(buildContext))
					.executes(ctx -> {
						var vl = ServerWorldSessionData.of(ctx.getSource().getServer());
						var value = key.type().get(ctx, "value");
						vl.dataMap.set(key, Cast.to(value));
						return 1;
					})
				)
			);

			cmd.then(Commands.literal("reset").executes(ctx -> {
				ctx.getSource().getServer().reset(key);
				return 1;
			}));

			command.then(cmd);
		}
	});
}
