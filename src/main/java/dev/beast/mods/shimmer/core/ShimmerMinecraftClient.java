package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.math.Vec2d;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector4f;

import java.util.List;

public interface ShimmerMinecraftClient extends ShimmerMinecraftEnvironment, ShimmerClientEntityContainer {
	default Minecraft shimmer$self() {
		return (Minecraft) this;
	}

	@ApiStatus.Internal
	default void shimmer$renderSetup(RenderLevelStageEvent event, float delta) {
	}

	default Vec2d shimmer$getCameraShakeOffset() {
		return Vec2d.ZERO;
	}

	@Override
	default List<? extends Player> shimmer$getPlayers() {
		var player = shimmer$self().player;
		return player == null ? List.of() : List.of(player);
	}

	default void shimmer$applyCameraShake(Camera camera, float delta) {
		var shake = shimmer$getCameraShakeOffset();

		if (shake != Vec2d.ZERO) {
			var vec = new Vector4f((float) shake.x, (float) shake.y, 0F, 1F).rotate(camera.rotation());
			camera.shimmer$setPosition(camera.getPosition().add(vec.x(), vec.y(), vec.z()));
		}
	}
}
