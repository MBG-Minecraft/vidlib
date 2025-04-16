package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.kmath.MovementType;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.server.command.EnumArgument;

public interface WindCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("wind", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("type", EnumArgument.enumArgument(MovementType.class))
			.then(Commands.argument("position", BlockPosArgument.blockPos())
				.then(Commands.argument("count", IntegerArgumentType.integer(1))
					.then(Commands.argument("radius", FloatArgumentType.floatArg(0.1F))
						.then(Commands.argument("yaw", AngleArgument.angle())
							.then(Commands.argument("pitch", FloatArgumentType.floatArg())
								.then(Commands.argument("ground", BoolArgumentType.bool())
									.executes(ctx -> spawn(ctx.getSource(),
										ctx.getArgument("type", MovementType.class),
										BlockPosArgument.getBlockPos(ctx, "position"),
										IntegerArgumentType.getInteger(ctx, "count"),
										FloatArgumentType.getFloat(ctx, "radius"),
										AngleArgument.getAngle(ctx, "yaw"),
										FloatArgumentType.getFloat(ctx, "pitch"),
										BoolArgumentType.getBool(ctx, "ground")
									))
								)
							)
						)
					)
				)
			)
		)
	);

	static int spawn(CommandSourceStack source, MovementType type, BlockPos position, int count, float radius, float yaw, float pitch, boolean ground) {
		source.getLevel().windParticles(source.getLevel().random, new WindData(new WindParticleOptions(100, ground, 1F), new ParticleMovementData(type, position, count, radius, 0F, Rotation.deg(yaw, pitch))));
		return 1;
	}
}
