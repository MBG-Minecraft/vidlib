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
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface ShimmerEntityContainer {
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		throw new NoMixinException();
	}

	default List<Entity> shimmer$getEntities() {
		return List.of();
	}

	default List<? extends Player> shimmer$getPlayers() {
		return List.of();
	}

	default void s2c(Packet<?> packet) {
		for (var player : shimmer$getPlayers()) {
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.connection.send(packet);
			}
		}
	}

	default void s2c(CustomPacketPayload packet) {
		s2c(new ClientboundCustomPayloadPacket(packet));
	}

	default void c2s(Packet<?> packet) {
	}

	default void c2s(CustomPacketPayload packet) {
		c2s(new ServerboundCustomPayloadPacket(packet));
	}

	default void tell(Component message) {
		for (var player : shimmer$getPlayers()) {
			player.sendSystemMessage(message);
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

	default void playCutscene(ResourceLocation id, WorldNumberVariables variables) {
		var cutscene = Cutscene.SERVER.get(id);

		if (cutscene != null) {
			playCutscene(cutscene, variables);
		}
	}

	default void stopCutscene() {
		s2c(StopCutscenePayload.INSTANCE);
	}

	default void shakeCamera(CameraShake shake) {
		s2c(new ShakeCameraPayload(shake));
	}

	default void stopCameraShaking() {
		s2c(StopCameraShakingPayload.INSTANCE);
	}

	default void setPostEffect(ResourceLocation id) {
		s2c(new SetPostEffectPayload(id));
	}
}
