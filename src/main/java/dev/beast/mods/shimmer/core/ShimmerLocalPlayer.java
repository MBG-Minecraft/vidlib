package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public interface ShimmerLocalPlayer extends ShimmerClientPlayer {
	@Override
	default ShimmerClientSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}

	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToServer(packet);
	}

	@Override
	default void playCutscene(Cutscene cutscene) {
		shimmer$getEnvironment().playCutscene(cutscene);
	}

	@Override
	default void stopCutscene() {
		shimmer$getEnvironment().stopCutscene();
	}

	@Override
	default void shakeCamera(CameraShake shake) {
		shimmer$getEnvironment().shakeCamera(shake);
	}

	@Override
	default void stopCameraShaking() {
		shimmer$getEnvironment().stopCameraShaking();
	}

	@Override
	default void setPostEffect(ResourceLocation id) {
		shimmer$getEnvironment().setPostEffect(id);
	}
}
