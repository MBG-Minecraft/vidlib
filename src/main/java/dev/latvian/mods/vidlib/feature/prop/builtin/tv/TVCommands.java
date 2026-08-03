package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface TVCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("tv", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("play")
			.then(Commands.literal("url")
				.then(Commands.argument("url", StringArgumentType.greedyString())
					.executes(ctx -> {
						broadcast(ctx, TVPlayPayload.uri(StringArgumentType.getString(ctx, "url"), 0));
						return 1;
					})
				)
			)
			.then(Commands.literal("file")
				.then(Commands.argument("file", StringArgumentType.greedyString())
					.executes(ctx -> {
						broadcast(ctx, TVPlayPayload.file(StringArgumentType.getString(ctx, "file"), 0));
						return 1;
					})
				)
			)
			.then(Commands.literal("looping")
				.then(Commands.literal("url")
					.then(Commands.argument("url", StringArgumentType.greedyString())
						.executes(ctx -> {
							broadcast(ctx, TVPlayPayload.uri(StringArgumentType.getString(ctx, "url"), TVPlayPayload.LOOPING));
							return 1;
						})
					)
				)
				.then(Commands.literal("file")
					.then(Commands.argument("file", StringArgumentType.greedyString())
						.executes(ctx -> {
							broadcast(ctx, TVPlayPayload.file(StringArgumentType.getString(ctx, "file"), TVPlayPayload.LOOPING));
							return 1;
						})
					)
				)
			)
			.executes(ctx -> {
				broadcast(ctx, new TVControlPayload(0));
				return 1;
			})
		)
		.then(Commands.literal("pause")
			.executes(ctx -> {
				broadcast(ctx, new TVControlPayload(1));
				return 1;
			})
		)
		.then(Commands.literal("stop")
			.executes(ctx -> {
				broadcast(ctx, new TVControlPayload(3));
				return 1;
			})
		)
	);

	private static void broadcast(CommandContext<CommandSourceStack> ctx, SimplePacketPayload packet) {
		ctx.getSource().getServer().s2c(packet);
	}
}
