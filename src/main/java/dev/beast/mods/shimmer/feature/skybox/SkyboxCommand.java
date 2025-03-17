package dev.beast.mods.shimmer.feature.skybox;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.misc.InternalServerData;
import dev.beast.mods.shimmer.util.registry.ShimmerResourceLocationArgument;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public interface SkyboxCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("skybox", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("skybox", ShimmerResourceLocationArgument.id())
				.suggests(SkyboxData.SUGGESTION_PROVIDER)
				.executes(ctx -> skybox(ctx.getSource().getServer(), ShimmerResourceLocationArgument.getId(ctx, "skybox")))
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> skybox(ctx.getSource().getServer(), null))
		)
	);

	private static int skybox(MinecraftServer server, @Nullable ResourceLocation skybox) {
		server.getServerData().set(InternalServerData.SKYBOX, skybox == null ? Skyboxes.DEFAULT : skybox);
		return 1;
	}
}
