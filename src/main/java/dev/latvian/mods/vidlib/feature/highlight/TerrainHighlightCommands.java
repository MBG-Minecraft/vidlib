package dev.latvian.mods.vidlib.feature.highlight;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.shape.CylinderShape;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.math.worldvector.FixedWorldVector;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;

public interface TerrainHighlightCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("terrain-highlight", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("position", Vec3Argument.vec3())
			.then(Commands.argument("shape", CommandDataType.SHAPE.argument(buildContext))
				.then(Commands.argument("color", CommandDataType.GRADIENT.argument(buildContext))
					.then(Commands.argument("duration", IntegerArgumentType.integer(0))
						.executes(ctx -> {
							ctx.getSource().getLevel().addTerrainHighlight(new TerrainHighlight(
								WorldVector.fixed(Vec3Argument.getVec3(ctx, "position")),
								CommandDataType.SHAPE.get(ctx, "shape"),
								CommandDataType.GRADIENT.get(ctx, "color"),
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
								new Color(0xCCFFDD00).withAlpha(100).gradient(Color.RED.withAlpha(100), Easing.QUAD_IN),
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
