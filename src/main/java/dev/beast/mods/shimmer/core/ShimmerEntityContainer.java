package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.camerashake.ShakeCameraPayload;
import dev.beast.mods.shimmer.feature.camerashake.StopCameraShakingPayload;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.cutscene.PlayCutscenePayload;
import dev.beast.mods.shimmer.feature.cutscene.StopCutscenePayload;
import dev.beast.mods.shimmer.feature.misc.SetPostEffectPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
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

	default void send(CustomPacketPayload packet) {
		var p = new ClientboundCustomPayloadPacket(packet);

		for (var player : shimmer$getPlayers()) {
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.connection.send(p);
			}
		}
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

	default void playCutscene(Cutscene cutscene) {
		if (!cutscene.steps.isEmpty()) {
			send(new PlayCutscenePayload(cutscene));
		}
	}

	default void playCutscene(ResourceLocation id) {
		var cutscene = Cutscene.SERVER.get(id);

		if (cutscene != null) {
			playCutscene(cutscene);
		}
	}

	default void stopCutscene() {
		send(StopCutscenePayload.INSTANCE);
	}

	default void shakeCamera(CameraShake shake) {
		send(new ShakeCameraPayload(shake));
	}

	default void stopCameraShaking() {
		send(StopCameraShakingPayload.INSTANCE);
	}

	default void setPostEffect(ResourceLocation id) {
		send(new SetPostEffectPayload(id));
	}
}
