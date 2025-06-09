package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.klib.color.GradientReference;
import dev.latvian.mods.klib.math.MovementType;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.gradient.GradientCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.command.EnumArgument;

public interface FireCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("fire", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("type", EnumArgument.enumArgument(MovementType.class))
			.then(Commands.argument("position", BlockPosArgument.blockPos())
				.then(Commands.argument("count", IntegerArgumentType.integer(1))
					.then(Commands.argument("radius", FloatArgumentType.floatArg(0.1F))
						.then(Commands.argument("yaw", AngleArgument.angle())
							.then(Commands.argument("pitch", FloatArgumentType.floatArg())
								.then(Commands.argument("gradient", ResourceLocationArgument.id())
									.suggests(GradientCommand.SUGGESTION_PROVIDER)
									.executes(ctx -> spawn(ctx.getSource(),
										ctx.getArgument("type", MovementType.class),
										BlockPosArgument.getBlockPos(ctx, "position"),
										IntegerArgumentType.getInteger(ctx, "count"),
										FloatArgumentType.getFloat(ctx, "radius"),
										AngleArgument.getAngle(ctx, "yaw"),
										FloatArgumentType.getFloat(ctx, "pitch"),
										ResourceLocationArgument.getId(ctx, "gradient")
									))
								)
							)
						)
					)
				)
			)
		)
	);

	static int spawn(CommandSourceStack source, MovementType type, BlockPos position, int count, float radius, float yaw, float pitch, ResourceLocation gradient) {
		source.getLevel().fireParticles(source.getLevel().random, new FireData(new FireParticleOptions(new GradientReference(gradient), 60, 1F), new ParticleMovementData(type, Vec3.atCenterOf(position), count, radius, 0F, Rotation.deg(yaw, pitch))));
		return 1;
	}
}
