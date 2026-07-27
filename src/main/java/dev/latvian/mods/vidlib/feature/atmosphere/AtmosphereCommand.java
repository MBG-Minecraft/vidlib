package dev.latvian.mods.vidlib.feature.atmosphere;

import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

public interface AtmosphereCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("atmosphere", (command, buildContext) -> command
		.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.literal("set")
			.then(Commands.argument("atmosphere", Atmosphere.DATA_TYPE.argument(buildContext))
				.executes(ctx -> atmosphere(ctx.getSource().getServer(), Atmosphere.DATA_TYPE.get(ctx, "atmosphere")))
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> atmosphere(ctx.getSource().getServer(), null))
		)
	);

	private static int atmosphere(MinecraftServer server, Ref<Atmosphere> atmosphere) {
		server.setAtmosphere(atmosphere);
		return 1;
	}
}
