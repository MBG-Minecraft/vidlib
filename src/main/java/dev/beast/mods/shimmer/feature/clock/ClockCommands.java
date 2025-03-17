package dev.beast.mods.shimmer.feature.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.util.registry.VideoResourceLocationArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface ClockCommands {
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = VideoResourceLocationArgument.registerSuggestionProvider(Shimmer.id("clock"), () -> ClockInstance.SERVER.getMap().keySet());

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clock", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("start")
			.then(Commands.argument("id", VideoResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> ClockInstance.command(ctx, ClockInstance::start))
			)
		)
		.then(Commands.literal("stop")
			.then(Commands.argument("id", VideoResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> ClockInstance.command(ctx, ClockInstance::stop))
			)
		)
		.then(Commands.literal("reset")
			.then(Commands.argument("id", VideoResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> ClockInstance.command(ctx, ClockInstance::reset))
			)
		)
		.then(Commands.literal("restart")
			.then(Commands.argument("id", VideoResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> ClockInstance.command(ctx, ClockInstance::restart))
			)
		)
		.then(Commands.literal("set")
			.then(Commands.argument("id", VideoResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.then(Commands.argument("tick", IntegerArgumentType.integer(0))
					.executes(ctx -> ClockInstance.command(ctx, (instance, server) -> instance.setTick(server, IntegerArgumentType.getInteger(ctx, "tick"))))
				)
			)
		)
	);
}
