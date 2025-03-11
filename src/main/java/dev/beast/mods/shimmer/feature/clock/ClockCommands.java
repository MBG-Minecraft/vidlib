package dev.beast.mods.shimmer.feature.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;

public interface ClockCommands {
	@AutoRegister
	ServerCommandHolder HOLDER = new ServerCommandHolder("clock", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("start")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.executes(ctx -> ClockInstance.command(ctx, ClockInstance::start))
			)
		)
		.then(Commands.literal("stop")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.executes(ctx -> ClockInstance.command(ctx, ClockInstance::stop))
			)
		)
		.then(Commands.literal("reset")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.executes(ctx -> ClockInstance.command(ctx, ClockInstance::reset))
			)
		)
		.then(Commands.literal("restart")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.executes(ctx -> ClockInstance.command(ctx, ClockInstance::restart))
			)
		)
		.then(Commands.literal("set")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.then(Commands.argument("tick", IntegerArgumentType.integer(0))
					.executes(ctx -> ClockInstance.command(ctx, (instance, server) -> instance.setTick(server, IntegerArgumentType.getInteger(ctx, "tick"))))
				)
			)
		)
	);
}
