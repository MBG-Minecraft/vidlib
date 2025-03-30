package dev.beast.mods.shimmer.feature.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface ClockCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clock", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("id", Clock.KNOWN_CODEC.argument(buildContext))
				.suggests(Clock.REGISTRY.suggestionProvider)
				.then(Commands.argument("second", IntegerArgumentType.integer(0))
					.executes(ctx -> set(ctx.getSource(), Clock.KNOWN_CODEC.get(ctx, "id"), IntegerArgumentType.getInteger(ctx, "second")))
				)
			)
		)
		.then(Commands.literal("reset")
			.then(Commands.argument("id", Clock.KNOWN_CODEC.argument(buildContext))
				.suggests(Clock.REGISTRY.suggestionProvider)
				.executes(ctx -> reset(ctx.getSource(), Clock.KNOWN_CODEC.get(ctx, "id")))
			)
		)
	);

	static int set(CommandSourceStack source, Clock clock, int second) {
		source.getServer().setClock(clock.id(), second);
		return 1;
	}

	static int reset(CommandSourceStack source, Clock clock) {
		source.getServer().resetClock(clock.id());
		return 1;
	}
}
