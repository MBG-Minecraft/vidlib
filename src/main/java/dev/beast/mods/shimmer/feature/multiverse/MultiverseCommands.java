package dev.beast.mods.shimmer.feature.multiverse;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MultiverseCommands {
	@AutoRegister
	public static final ServerCommandHolder COMMAND = new ServerCommandHolder("multiverse", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("reload")
			.executes(ctx -> {
				// ctx.getSource().getLevel().shimmer$reloadChunks();
				ctx.getSource().sendFailure(Component.literal("WIP"));
				return 1;
			})
		)
	);
}
