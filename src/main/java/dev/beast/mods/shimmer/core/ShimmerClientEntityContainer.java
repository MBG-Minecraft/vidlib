package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public interface ShimmerClientEntityContainer extends ShimmerEntityContainer {
	@Override
	default void s2c(Packet<?> packet) {
	}

	@Override
	default void c2s(Packet<?> packet) {
		Minecraft.getInstance().getConnection().send(packet);
	}

	@Override
	default void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		shimmer$getEnvironment().playCutscene(cutscene, variables);
	}

	@Override
	default void playCutscene(ResourceLocation id, WorldNumberVariables variables) {
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
