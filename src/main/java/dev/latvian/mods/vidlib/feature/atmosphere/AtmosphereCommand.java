package dev.latvian.mods.vidlib.feature.atmosphere;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;

public interface AtmosphereCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("atmosphere", (command, buildContext) -> command
		.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.literal("set")
			.then(Commands.argument("atmosphere", Atmosphere.REGISTRY.keyArgument(buildContext))
				.executes(ctx -> atmosphere(ctx.getSource().getServer(), Atmosphere.REGISTRY.getKey(ctx, "atmosphere")))
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> atmosphere(ctx.getSource().getServer(), null))
		)
	);

	private static int atmosphere(MinecraftServer server, ResourceKey<Atmosphere> atmosphere) {
		server.setAtmosphere(atmosphere);
		return 1;
	}
}
