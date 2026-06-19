package dev.latvian.mods.vidlib.feature.multiverse;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MultiverseCommands {
	@AutoRegister
	public static final ServerCommandHolder COMMAND = new ServerCommandHolder("multiverse", (command, buildContext) -> command
		.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.literal("reload")
			.executes(ctx -> {
				// ctx.getSource().getLevel().vl$reloadChunks();
				ctx.getSource().sendFailure(Component.literal("WIP"));
				return 1;
			})
		)
	);
}
