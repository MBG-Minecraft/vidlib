package dev.latvian.mods.vidlib.feature.waypoint;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class WaypointCommands {
	@AutoRegister
	public static final ServerCommandHolder COMMAND = new ServerCommandHolder("waypoint", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("add")
			.then(Commands.argument("id", StringArgumentType.word())
				.then(Commands.argument("position", Vec3Argument.vec3())
					.then(Commands.argument("label", ComponentArgument.textComponent(buildContext))
						.executes(ctx -> add(
							ctx.getSource(),
							StringArgumentType.getString(ctx, "id"),
							Vec3Argument.getVec3(ctx, "position"),
							ComponentArgument.getResolvedComponent(ctx, "label")
						))
					)
				)
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("id", StringArgumentType.word())
				.executes(ctx -> remove(ctx.getSource(), StringArgumentType.getString(ctx, "id")))
			)
		)
	);

	private static int add(CommandSourceStack source, String id, Vec3 position, Component label) {
		source.getServer().addWaypoints(List.of(new Waypoint.Builder()
			.id(id)
			.position(KVector.of(position))
			.label(label)
			.tint(Color.hsb(source.getLevel().random.nextFloat(), 1F, 1F, 255))
			.build()
		));

		return 0;
	}

	public static int remove(CommandSourceStack source, String id) {
		source.getServer().removeWaypoints(Set.of(id));
		return 1;
	}
}
