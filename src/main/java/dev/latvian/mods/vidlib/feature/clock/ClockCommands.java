package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;

public interface ClockCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clock", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.then(Commands.argument("second", IntegerArgumentType.integer(0))
					.executes(ctx -> set(ctx.getSource(), ResourceLocationArgument.getId(ctx, "id"), IntegerArgumentType.getInteger(ctx, "second")))
				)
			)
		)
		.then(Commands.literal("reset")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.executes(ctx -> reset(ctx.getSource(), ResourceLocationArgument.getId(ctx, "id")))
			)
		)
	);

	static int set(CommandSourceStack source, ResourceLocation clock, int second) {
		source.getServer().setClock(clock, second);
		return 1;
	}

	static int reset(CommandSourceStack source, ResourceLocation clock) {
		source.getServer().resetClock(clock);
		return 1;
	}
}
