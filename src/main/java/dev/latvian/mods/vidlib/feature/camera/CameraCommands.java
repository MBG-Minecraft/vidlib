package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;

public interface CameraCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("camera", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("shake")
			.then(Commands.literal("add")
				.then(Commands.literal("basic")
					.then(Commands.argument("player", EntityArgument.players())
						.then(Commands.argument("data", ScreenShake.COMMAND.argument(buildContext))
							.executes(ctx -> addBasic(EntityArgument.getPlayers(ctx, "player"), ScreenShake.COMMAND.get(ctx, "data")))
						)
						.executes(ctx -> addBasic(EntityArgument.getPlayers(ctx, "player"), ScreenShake.DEFAULT))
					)
				)
				.then(Commands.literal("at")
					.then(Commands.argument("player", EntityArgument.players())
						.then(Commands.argument("at", Vec3Argument.vec3())
							.then(Commands.argument("max-distance", DoubleArgumentType.doubleArg())
								.then(Commands.argument("data", ScreenShake.COMMAND.argument(buildContext))
									.executes(ctx -> addAt(EntityArgument.getPlayers(ctx, "player"), Vec3Argument.getVec3(ctx, "at"), DoubleArgumentType.getDouble(ctx, "max-distance"), ScreenShake.COMMAND.get(ctx, "data")))
								)
								.executes(ctx -> addAt(EntityArgument.getPlayers(ctx, "player"), Vec3Argument.getVec3(ctx, "at"), DoubleArgumentType.getDouble(ctx, "max-distance"), ScreenShake.DEFAULT))
							)
						)
					)
				)
			)
			.then(Commands.literal("clear")
				.then(Commands.argument("player", EntityArgument.players())
					.executes(ctx -> clear(EntityArgument.getPlayers(ctx, "player")))
				)
			)
		)
		.then(Commands.literal("mode")
			.then(Commands.literal("normal")
				.then(Commands.argument("player", EntityArgument.players())
					.executes(ctx -> mode(EntityArgument.getPlayers(ctx, "player"), 0))
				)
				.executes(ctx -> mode(List.of(ctx.getSource().getPlayerOrException()), 0))
			)
			.then(Commands.literal("detached")
				.then(Commands.argument("player", EntityArgument.players())
					.executes(ctx -> mode(EntityArgument.getPlayers(ctx, "player"), 1))
				)
				.executes(ctx -> mode(List.of(ctx.getSource().getPlayerOrException()), 1))
			)
			.then(Commands.literal("free")
				.then(Commands.argument("player", EntityArgument.players())
					.executes(ctx -> mode(EntityArgument.getPlayers(ctx, "player"), 2))
				)
				.executes(ctx -> mode(List.of(ctx.getSource().getPlayerOrException()), 2))
			)
			.then(Commands.literal("swap")
				.then(Commands.argument("player", EntityArgument.players())
					.executes(ctx -> mode(EntityArgument.getPlayers(ctx, "player"), 3))
				)
				.executes(ctx -> mode(List.of(ctx.getSource().getPlayerOrException()), 3))
			)
		)
	);

	static int addBasic(Collection<ServerPlayer> players, ScreenShake data) {
		for (var player : players) {
			player.screenShake(data);
		}

		return 1;
	}

	static int addAt(Collection<ServerPlayer> players, Vec3 at, double maxDistance, ScreenShake data) {
		for (var player : players) {
			player.screenShake(data, at, maxDistance);
		}

		return 1;
	}

	static int clear(Collection<ServerPlayer> players) {
		for (var player : players) {
			player.stopScreenShake();
		}

		return 1;
	}

	static int mode(Collection<ServerPlayer> players, int mode) {
		for (var player : players) {
			player.setCameraMode(mode);
		}

		return 1;
	}
}
