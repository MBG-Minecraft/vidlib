package dev.beast.mods.shimmer.feature.particle;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.math.Easing;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;

public interface WindCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("wind", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("angled")
			.then(Commands.argument("yaw", AngleArgument.angle())
				.then(Commands.argument("position", BlockPosArgument.blockPos())
					.then(Commands.argument("count", IntegerArgumentType.integer(1))
						.then(Commands.argument("radius", FloatArgumentType.floatArg(0.1F))
							.then(Commands.argument("ground", BoolArgumentType.bool())
								.then(Commands.argument("air", BoolArgumentType.bool())
									.executes(ctx -> angledWind(ctx.getSource(),
										BlockPosArgument.getBlockPos(ctx, "position"),
										IntegerArgumentType.getInteger(ctx, "count"),
										FloatArgumentType.getFloat(ctx, "radius"),
										AngleArgument.getAngle(ctx, "yaw"),
										BoolArgumentType.getBool(ctx, "ground")
									))
								)
							)
						)
					)
				)
			)
		)
		.then(Commands.literal("circular")
			.then(Commands.argument("position", BlockPosArgument.blockPos())
				.then(Commands.argument("count", IntegerArgumentType.integer(1))
					.then(Commands.argument("radius", FloatArgumentType.floatArg(0.1F))
						.then(Commands.argument("ground", BoolArgumentType.bool())
							.then(Commands.argument("air", BoolArgumentType.bool())
								.executes(ctx -> circularWind(ctx.getSource(),
									BlockPosArgument.getBlockPos(ctx, "position"),
									IntegerArgumentType.getInteger(ctx, "count"),
									FloatArgumentType.getFloat(ctx, "radius"),
									BoolArgumentType.getBool(ctx, "ground")
								))
							)
						)
					)
				)
			)
		)
		.then(Commands.literal("square")
			.then(Commands.argument("position", BlockPosArgument.blockPos())
				.then(Commands.argument("count", IntegerArgumentType.integer(1))
					.then(Commands.argument("radius", FloatArgumentType.floatArg(0.1F))
						.then(Commands.argument("ground", BoolArgumentType.bool())
							.executes(ctx -> squareWind(ctx.getSource(),
								BlockPosArgument.getBlockPos(ctx, "position"),
								IntegerArgumentType.getInteger(ctx, "count"),
								FloatArgumentType.getFloat(ctx, "radius"),
								BoolArgumentType.getBool(ctx, "ground")
							))
						)
					)
				)
			)
		)
	);

	static int angledWind(CommandSourceStack source, BlockPos position, int count, float radius, float yaw, boolean ground) {
		source.getLevel().spawnWindParticles(source.getLevel().random, new WindData(new WindParticleOptions(100, ground, Easing.SINE_OUT), WindType.ANGLED, position, count, radius, yaw));
		return 1;
	}

	static int circularWind(CommandSourceStack source, BlockPos position, int count, float radius, boolean ground) {
		source.getLevel().spawnWindParticles(source.getLevel().random, new WindData(new WindParticleOptions(100, ground, Easing.SINE_OUT), WindType.CIRCULAR, position, count, radius, 0F));
		return 1;
	}

	static int squareWind(CommandSourceStack source, BlockPos position, int count, float radius, boolean ground) {
		source.getLevel().spawnWindParticles(source.getLevel().random, new WindData(new WindParticleOptions(100, ground, Easing.SINE_OUT), WindType.SQUARE, position, count, radius, 0F));
		return 1;
	}
}
