package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public interface ClockCommands {
	List<ResourceLocation> CLOCK_IDS = new ArrayList<>();
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ID.registerSuggestionProvider(VidLib.id("clock"), () -> CLOCK_IDS);

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clock", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.then(Commands.argument("second", IntegerArgumentType.integer(0))
					.executes(ctx -> set(ctx.getSource(), ResourceLocationArgument.getId(ctx, "id"), IntegerArgumentType.getInteger(ctx, "second")))
				)
			)
		)
		.then(Commands.literal("reset")
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
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
