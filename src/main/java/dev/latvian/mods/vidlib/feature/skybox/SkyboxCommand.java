package dev.latvian.mods.vidlib.feature.skybox;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;

public interface SkyboxCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("skybox", (command, buildContext) -> command
		.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
		.then(Commands.literal("set")
			.then(Commands.argument("skybox", SkyboxData.ID_DATA_TYPE.argument(buildContext))
				.executes(ctx -> skybox(ctx.getSource().getServer(), SkyboxData.ID_DATA_TYPE.get(ctx, "skybox")))
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> skybox(ctx.getSource().getServer(), Skyboxes.VANILLA))
		)
	);

	private static int skybox(MinecraftServer server, Identifier skybox) {
		server.setSkybox(skybox);
		return 1;
	}
}
