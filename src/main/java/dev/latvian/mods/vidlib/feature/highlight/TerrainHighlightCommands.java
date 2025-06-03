package dev.latvian.mods.vidlib.feature.highlight;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.PairGradient;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.kmath.shape.CylinderShape;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import dev.latvian.mods.vidlib.math.worldvector.FixedWorldVector;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;

public interface TerrainHighlightCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("terrain-highlight", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("position", Vec3Argument.vec3())
			.then(Commands.argument("shape", RegisteredDataType.SHAPE.argument(buildContext))
				.then(Commands.argument("color", RegisteredDataType.GRADIENT.argument(buildContext))
					.then(Commands.argument("duration", IntegerArgumentType.integer(0))
						.executes(ctx -> {
							ctx.getSource().getLevel().addTerrainHighlight(new TerrainHighlight(
								WorldVector.fixed(Vec3Argument.getVec3(ctx, "position")),
								RegisteredDataType.SHAPE.get(ctx, "shape"),
								RegisteredDataType.GRADIENT.get(ctx, "color"),
								FixedWorldVector.ONE.instance(),
								IntegerArgumentType.getInteger(ctx, "duration")
							));

							return 1;
						})
					)
				)
			)
		)
		.then(Commands.literal("danger")
			.then(Commands.argument("position", Vec3Argument.vec3())
				.then(Commands.argument("radius", FloatArgumentType.floatArg(0F))
					.then(Commands.argument("duration", IntegerArgumentType.integer(0))
						.executes(ctx -> {
							var pos = WorldVector.fixed(Vec3Argument.getVec3(ctx, "position"));
							var shape = new CylinderShape(FloatArgumentType.getFloat(ctx, "radius"), 0F);
							var duration = IntegerArgumentType.getInteger(ctx, "duration");

							ctx.getSource().getLevel().addTerrainHighlight(new TerrainHighlight(
								pos,
								shape,
								new PairGradient(new Color(0xCCFFDD00).withAlpha(100), Color.RED.withAlpha(100), Easing.QUAD_IN),
								FixedWorldVector.ONE.instance(),
								duration
							));

							ctx.getSource().getLevel().addTerrainHighlight(new TerrainHighlight(
								pos,
								shape,
								Color.RED.withAlpha(100),
								FixedWorldVector.ZERO.instance().interpolate(Easing.QUAD_IN, FixedWorldVector.ONE.instance()),
								duration
							));

							return 1;
						})
					)
				)
			)
		)
	);
}
