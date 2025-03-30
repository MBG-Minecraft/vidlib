package dev.beast.mods.shimmer.feature.camerashake;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public interface CameraShakeCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("camera-shake", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("add")
			.then(Commands.literal("basic")
				.then(Commands.argument("player", EntityArgument.players())
					.then(Commands.argument("data", CameraShake.KNOWN_CODEC.argument(buildContext))
						.executes(ctx -> addBasic(EntityArgument.getPlayers(ctx, "player"), CameraShake.KNOWN_CODEC.get(ctx, "data")))
					)
					.executes(ctx -> addBasic(EntityArgument.getPlayers(ctx, "player"), CameraShake.DEFAULT))
				)
			)
			.then(Commands.literal("at")
				.then(Commands.argument("player", EntityArgument.players())
					.then(Commands.argument("at", Vec3Argument.vec3())
						.then(Commands.argument("max-distance", DoubleArgumentType.doubleArg())
							.then(Commands.argument("data", CameraShake.KNOWN_CODEC.argument(buildContext))
								.executes(ctx -> addAt(EntityArgument.getPlayers(ctx, "player"), Vec3Argument.getVec3(ctx, "at"), DoubleArgumentType.getDouble(ctx, "max-distance"), CameraShake.KNOWN_CODEC.get(ctx, "data")))
							)
							.executes(ctx -> addAt(EntityArgument.getPlayers(ctx, "player"), Vec3Argument.getVec3(ctx, "at"), DoubleArgumentType.getDouble(ctx, "max-distance"), CameraShake.DEFAULT))
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
	);

	static int addBasic(Collection<ServerPlayer> players, CameraShake data) {
		for (var player : players) {
			player.shakeCamera(data);
		}

		return 1;
	}

	static int addAt(Collection<ServerPlayer> players, Vec3 at, double maxDistance, CameraShake data) {
		for (var player : players) {
			player.shakeCamera(data, at, maxDistance);
		}

		return 1;
	}

	static int clear(Collection<ServerPlayer> players) {
		for (var player : players) {
			player.stopCameraShaking();
		}

		return 1;
	}
}
