package dev.beast.mods.shimmer.feature.camerashake;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;

public interface CameraShakeCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("camera-shape", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("add")
			.then(Commands.argument("data", CameraShake.KNOWN_CODEC.argument(buildContext))
				.executes(ctx -> {
					ctx.getSource().getPlayerOrException().shakeCamera(CameraShake.KNOWN_CODEC.get(ctx, "data"));
					return 1;
				})
			)
		)
		.then(Commands.literal("clear")
			.executes(ctx -> {
				ctx.getSource().getPlayerOrException().stopCameraShaking();
				return 1;
			})
		)
	);
}
