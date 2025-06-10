package dev.latvian.mods.vidlib.feature.skybox;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public interface SkyboxCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("skybox", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("skybox", SkyboxData.COMMAND.argument(buildContext))
				.executes(ctx -> skybox(ctx.getSource().getServer(), SkyboxData.COMMAND.get(ctx, "skybox")))
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> skybox(ctx.getSource().getServer(), Skyboxes.DAY_WITH_CELESTIALS))
		)
	);

	private static int skybox(MinecraftServer server, ResourceLocation skybox) {
		server.setSkybox(skybox);
		return 1;
	}
}
