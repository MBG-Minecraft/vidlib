package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.camerashake.ShakeCameraPayload;
import dev.beast.mods.shimmer.feature.camerashake.StopCameraShakingPayload;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.cutscene.PlayCutscenePayload;
import dev.beast.mods.shimmer.feature.cutscene.StopCutscenePayload;
import dev.beast.mods.shimmer.feature.misc.SetPostEffectPayload;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ShimmerEntityContainer extends ShimmerS2CPacketConsumer, ShimmerC2SPacketConsumer {
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		throw new NoMixinException(this);
	}

	default List<Entity> shimmer$getEntities() {
		return List.of();
	}

	default List<? extends Player> shimmer$getPlayers() {
		return List.of();
	}

	@Override
	default void s2c(@Nullable Packet<? super ClientGamePacketListener> packet) {
		for (var player : shimmer$getPlayers()) {
			player.s2c(packet);
		}
	}

	@Override
	default void c2s(@Nullable Packet<? super ServerGamePacketListener> packet) {
	}

	default void tell(Component message) {
		for (var player : shimmer$getPlayers()) {
			player.displayClientMessage(message, false);
		}
	}

	default void tell(String message) {
		tell(Component.literal(message));
	}

	default void status(Component message) {
		for (var player : shimmer$getPlayers()) {
			player.displayClientMessage(message, true);
		}
	}

	default void status(String message) {
		status(Component.literal(message));
	}

	default void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		if (!cutscene.steps.isEmpty()) {
			s2c(new PlayCutscenePayload(cutscene, variables));
		}
	}

	default void stopCutscene() {
		s2c(StopCutscenePayload.INSTANCE);
	}

	default void shakeCamera(CameraShake shake) {
		if (!shake.skip()) {
			s2c(new ShakeCameraPayload(shake));
		}
	}

	default void shakeCamera(CameraShake shake, Vec3 source, double maxDistance) {
		for (var player : shimmer$getPlayers()) {
			shakeCamera(shake.atDistance(player.position(), source, maxDistance));
		}
	}

	default void stopCameraShaking() {
		s2c(StopCameraShakingPayload.INSTANCE);
	}

	default void setPostEffect(ResourceLocation id) {
		s2c(new SetPostEffectPayload(id));
	}
}
