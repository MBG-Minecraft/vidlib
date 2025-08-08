package dev.latvian.mods.vidlib.feature.prop.builtin.highlight;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.kvector.DynamicKVector;
import dev.latvian.mods.vidlib.math.kvector.KVector;
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
							var pos = Vec3Argument.getVec3(ctx, "position");
							var shape = CommandDataType.SHAPE.get(ctx, "shape");
							var color = CommandDataType.GRADIENT.get(ctx, "color");
							var duration = IntegerArgumentType.getInteger(ctx, "duration");

							ctx.getSource().getLevel().getProps().add(TerrainHighlightProp.TYPE, prop -> {
								prop.setPos(pos);
								prop.shape = shape;
								prop.color = color;
								prop.scale = KVector.ONE;
								prop.lifespan = duration;
							});

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
							var pos = Vec3Argument.getVec3(ctx, "position");
							var scalen = KNumber.of(FloatArgumentType.getFloat(ctx, "radius"));
							var scale = new DynamicKVector(scalen, KNumber.ZERO, scalen);
							var duration = IntegerArgumentType.getInteger(ctx, "duration");

							ctx.getSource().getLevel().getProps().add(TerrainHighlightProp.TYPE, prop -> {
								prop.setPos(pos);
								prop.color = new Color(0xCCFFDD00).withAlpha(100).gradient(Color.RED.withAlpha(100), Easing.QUAD_IN);
								prop.scale = scale;
								prop.lifespan = duration;
							});

							ctx.getSource().getLevel().getProps().add(TerrainHighlightProp.TYPE, prop -> {
								prop.setPos(pos);
								prop.color = Color.RED.withAlpha(100);
								prop.scale = KVector.ZERO.interpolate(Easing.QUAD_IN, scale);
								prop.lifespan = duration;
							});

							return 1;
						})
					)
				)
			)
		)
	);
}
