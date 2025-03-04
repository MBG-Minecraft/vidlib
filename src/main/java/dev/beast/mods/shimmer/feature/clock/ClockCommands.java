package dev.beast.mods.shimmer.feature.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;

public interface ClockCommands {
	static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
		return Commands.literal("clock")
			.then(Commands.literal("start")
				.then(Commands.argument("id", ResourceLocationArgument.id())
					.executes(ctx -> Clock.forCommand(ctx, ClockInstance::start))
				)
			)
			.then(Commands.literal("stop")
				.then(Commands.argument("id", ResourceLocationArgument.id())
					.executes(ctx -> Clock.forCommand(ctx, ClockInstance::stop))
				)
			)
			.then(Commands.literal("reset")
				.then(Commands.argument("id", ResourceLocationArgument.id())
					.executes(ctx -> Clock.forCommand(ctx, ClockInstance::reset))
				)
			)
			.then(Commands.literal("set")
				.then(Commands.argument("id", ResourceLocationArgument.id())
					.then(Commands.argument("tick", IntegerArgumentType.integer(0))
						.executes(ctx -> Clock.forCommand(ctx, (instance, server) -> instance.setTick(server, IntegerArgumentType.getInteger(ctx, "tick"))))
					)
				)
			);
	}
}
