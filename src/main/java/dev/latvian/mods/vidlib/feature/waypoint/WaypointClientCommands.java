package dev.latvian.mods.vidlib.feature.waypoint;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;

import java.util.ArrayList;

public interface WaypointClientCommands {
	@ClientAutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("waypoint-client", (command, buildContext) -> {
		command.then(Commands.literal("add")
			.then(Commands.argument("id", StringArgumentType.word())
				.then(Commands.argument("position", Vec3Argument.vec3())
					.then(Commands.argument("label", ComponentArgument.textComponent(buildContext))
						.executes(ctx -> {
							var mc = Minecraft.getInstance();

							if (mc.level != null && PlatformHelper.CURRENT.isReplayLevel(mc.level)) {
								var id = StringArgumentType.getString(ctx, "id");
								var position = Vec3Argument.getVec3(ctx, "position");
								var label = ComponentArgument.getResolvedComponent(ctx, "label");

								var waypoint = new Waypoint.Builder()
									.id(id)
									.position(KVector.of(position))
									.label(label)
									.distance(6D, 20D, 0D)
									.build();

								var session = mc.player.vl$sessionData();
								var current = new ArrayList<>(session.serverDataMap.get(InternalServerData.WAYPOINTS));
								current.add(waypoint);
								session.serverDataMap.setSuperOverride(InternalServerData.WAYPOINTS, current);
								return 1;
							} else {
								var cmd = ctx.getInput().replace("waypoint-client", "waypoint");
								mc.player.connection.sendCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
								return 1;
							}
						})
					)
				)
			)
		);

		command.then(Commands.literal("remove")
			.then(Commands.argument("id", StringArgumentType.word())
				.executes(ctx -> {
					var mc = Minecraft.getInstance();

					if (mc.level != null && PlatformHelper.CURRENT.isReplayLevel(mc.level)) {
						var id = StringArgumentType.getString(ctx, "id");

						var session = mc.player.vl$sessionData();
						var current = new ArrayList<>(session.serverDataMap.get(InternalServerData.WAYPOINTS));
						current.removeIf(w -> w.id().equals(id));
						session.serverDataMap.setSuperOverride(InternalServerData.WAYPOINTS, current);
						return 1;
					} else {
						var cmd = ctx.getInput().replace("waypoint-client", "waypoint");
						mc.player.connection.sendCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
						return 1;
					}
				})
			)
		);
	});
}
