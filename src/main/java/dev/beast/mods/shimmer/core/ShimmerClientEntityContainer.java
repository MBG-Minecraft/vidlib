package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface ShimmerClientEntityContainer extends ShimmerEntityContainer {
	@Override
	default void s2c(@Nullable Packet<? super ClientGamePacketListener> packet) {
	}

	@Override
	default void c2s(@Nullable Packet<? super ServerGamePacketListener> packet) {
		if (packet != null) {
			Minecraft.getInstance().getConnection().send(packet);
		}
	}

	@Override
	default void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		shimmer$getEnvironment().playCutscene(cutscene, variables);
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
	default void shakeCamera(CameraShake shake, Vec3 source, double maxDistance) {
		shimmer$getEnvironment().shakeCamera(shake, source, maxDistance);
	}

	@Override
	default void stopCameraShaking() {
		shimmer$getEnvironment().stopCameraShaking();
	}

	@Override
	default void setPostEffect(ResourceLocation id) {
		shimmer$getEnvironment().setPostEffect(id);
	}

	@Override
	default void shimmer$closeScreen() {
		shimmer$getEnvironment().shimmer$closeScreen();
	}

	@Override
	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		shimmer$getEnvironment().openYesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel);
	}

	@Override
	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable) {
		shimmer$getEnvironment().openNumberVotingScreen(extraData, title, subtitle, max, unavailable);
	}

	@Override
	default void endVote() {
		shimmer$getEnvironment().endVote();
	}
}
