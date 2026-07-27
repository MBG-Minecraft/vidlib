package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface ClockCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clock", (command, buildContext) -> command
		.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.literal("set")
			.then(Commands.argument("clock", Clock.DATA_TYPE.argument(buildContext))
				.then(Commands.argument("second", IntegerArgumentType.integer(0))
					.executes(ctx -> set(ctx.getSource(), Clock.DATA_TYPE.get(ctx, "clock"), IntegerArgumentType.getInteger(ctx, "second")))
				)
			)
		)
		.then(Commands.literal("reset")
			.then(Commands.argument("clock", Clock.DATA_TYPE.argument(buildContext))
				.executes(ctx -> reset(ctx.getSource(), Clock.DATA_TYPE.get(ctx, "clock")))
			)
		)
	);

	static int set(CommandSourceStack source, Ref<Clock> clock, int second) {
		source.getServer().setClock(clock, second);
		return 1;
	}

	static int reset(CommandSourceStack source, Ref<Clock> clock) {
		source.getServer().resetClock(clock);
		return 1;
	}
}
