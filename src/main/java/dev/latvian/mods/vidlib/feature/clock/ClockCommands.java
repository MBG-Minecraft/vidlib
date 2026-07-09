package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;

public interface ClockCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clock", (command, buildContext) -> command
		.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.literal("set")
			.then(Commands.argument("id", IdentifierArgument.id())
				.suggests(Clock.SUGGESTION_PROVIDER)
				.then(Commands.argument("second", IntegerArgumentType.integer(0))
					.executes(ctx -> set(ctx.getSource(), IdentifierArgument.getId(ctx, "id"), IntegerArgumentType.getInteger(ctx, "second")))
				)
			)
		)
		.then(Commands.literal("reset")
			.then(Commands.argument("id", IdentifierArgument.id())
				.suggests(Clock.SUGGESTION_PROVIDER)
				.executes(ctx -> reset(ctx.getSource(), IdentifierArgument.getId(ctx, "id")))
			)
		)
	);

	static int set(CommandSourceStack source, Identifier clock, int second) {
		source.getServer().setClock(clock, second);
		return 1;
	}

	static int reset(CommandSourceStack source, Identifier clock) {
		source.getServer().resetClock(clock);
		return 1;
	}
}
