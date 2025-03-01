package dev.beast.mods.shimmer.feature.camerashake;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.nbt.NbtOps;

public interface CameraShakeCommands {
	static LiteralArgumentBuilder<CommandSourceStack> createCommand(CommandBuildContext buildContext) {
		return Commands.literal("camera-shake")
			.then(Commands.literal("add")
				.then(Commands.argument("data", CompoundTagArgument.compoundTag())
					.executes(ctx -> {
						var tag = CompoundTagArgument.getCompoundTag(ctx, "data");
						var ops = buildContext.createSerializationContext(NbtOps.INSTANCE);
						ctx.getSource().getPlayerOrException().shakeCamera(CameraShake.CODEC.decode(ops, tag).getOrThrow().getFirst());
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
