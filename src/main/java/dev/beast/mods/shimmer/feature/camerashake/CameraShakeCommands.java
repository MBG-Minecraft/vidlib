package dev.beast.mods.shimmer.feature.camerashake;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.beast.mods.shimmer.util.KnownCodec;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface CameraShakeCommands {
	static LiteralArgumentBuilder<CommandSourceStack> createCommand(CommandBuildContext buildContext) {
		return Commands.literal("camera-shake")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(Commands.literal("add")
				.then(Commands.argument("data", KnownCodec.CAMERA_SHAKE.argument(buildContext))
					.executes(ctx -> {
						ctx.getSource().getPlayerOrException().shakeCamera(KnownCodec.CAMERA_SHAKE.get(ctx, "data"));
						return 1;
					})
				)
			)
			.then(Commands.literal("clear")
				.executes(ctx -> {
					ctx.getSource().getPlayerOrException().stopCameraShaking();
					return 1;
				})
			);
	}
}
