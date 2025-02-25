package dev.beast.mods.shimmer;

import com.mojang.brigadier.CommandDispatcher;
import dev.beast.mods.shimmer.content.clock.ClockContent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ShimmerCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("shimmer")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(ClockContent.createCommand())
		);
	}
}
