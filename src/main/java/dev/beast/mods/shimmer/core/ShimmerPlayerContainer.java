package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.bulk.RedrawChunkSectionsPayload;
import dev.beast.mods.shimmer.feature.camera.CameraShake;
import dev.beast.mods.shimmer.feature.camera.SetCameraModePayload;
import dev.beast.mods.shimmer.feature.camera.ShakeCameraAtPositionPayload;
import dev.beast.mods.shimmer.feature.camera.ShakeCameraPayload;
import dev.beast.mods.shimmer.feature.camera.StopCameraShakingPayload;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.cutscene.PlayCutscenePayload;
import dev.beast.mods.shimmer.feature.cutscene.StopCutscenePayload;
import dev.beast.mods.shimmer.feature.fade.Fade;
import dev.beast.mods.shimmer.feature.fade.ScreenFadePayload;
import dev.beast.mods.shimmer.feature.misc.CloseScreenPayload;
import dev.beast.mods.shimmer.feature.misc.SetPostEffectPayload;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.feature.particle.FireData;
import dev.beast.mods.shimmer.feature.particle.RemoveAllParticlesPayload;
import dev.beast.mods.shimmer.feature.particle.SpawnCubeParticlesPayload;
import dev.beast.mods.shimmer.feature.particle.SpawnFireParticlesPayload;
import dev.beast.mods.shimmer.feature.particle.SpawnTextParticlePayload;
import dev.beast.mods.shimmer.feature.particle.SpawnWindParticlesPayload;
import dev.beast.mods.shimmer.feature.particle.TextParticleOptions;
import dev.beast.mods.shimmer.feature.particle.WindData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticlesIdPayload;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticlesPayload;
import dev.beast.mods.shimmer.feature.sound.PositionedSoundData;
import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.sound.SoundPayload;
import dev.beast.mods.shimmer.feature.vote.StartNumberVotingPayload;
import dev.beast.mods.shimmer.feature.vote.StartYesNoVotingPayload;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface ShimmerPlayerContainer extends ShimmerS2CPacketConsumer, ShimmerC2SPacketConsumer {
	default List<? extends Player> shimmer$getS2CPlayers() {
		return List.of();
	}

	@Override
	default void s2c(@Nullable Packet<? super ClientGamePacketListener> packet) {
		for (var player : shimmer$getS2CPlayers()) {
			player.s2c(packet);
		}
	}

	@Override
	default void c2s(@Nullable Packet<? super ServerGamePacketListener> packet) {
		shimmer$getEnvironment().c2s(packet);
	}

	default void tell(Component message) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().tell(message);
		} else {
			for (var player : shimmer$getS2CPlayers()) {
				player.displayClientMessage(message, false);
			}
		}
	}

	default void tell(String message) {
		tell(Component.literal(message));
	}

	default void status(Component message) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().status(message);
		} else {
			for (var player : shimmer$getS2CPlayers()) {
				player.displayClientMessage(message, true);
			}
		}
	}

	default void status(String message) {
		status(Component.literal(message));
	}

	default void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().playCutscene(cutscene, variables);
		} else if (!cutscene.steps.isEmpty()) {
			s2c(new PlayCutscenePayload(cutscene, variables));
		}
	}

	default void stopCutscene() {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().stopCutscene();
		} else {
			s2c(StopCutscenePayload.INSTANCE);
		}
	}

	default void shakeCamera(CameraShake shake) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().shakeCamera(shake);
		} else if (!shake.skip()) {
			s2c(new ShakeCameraPayload(shake));
		}
	}

	default void shakeCamera(CameraShake shake, Vec3 source, double maxDistance) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().shakeCamera(shake, source, maxDistance);
		} else {
			s2c(new ShakeCameraAtPositionPayload(shake, source, maxDistance));
		}
	}

	default void stopCameraShaking() {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().stopCameraShaking();
		} else {
			s2c(StopCameraShakingPayload.INSTANCE);
		}
	}

	default void setCameraMode(int mode) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().setCameraMode(mode);
		} else {
			s2c(new SetCameraModePayload(mode));
		}
	}

	default void setPostEffect(ResourceLocation id) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().setPostEffect(id);
		} else {
			s2c(new SetPostEffectPayload(id));
		}
	}

	default void shimmer$closeScreen() {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().shimmer$closeScreen();
		} else {
			s2c(CloseScreenPayload.INSTANCE);
		}
	}

	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().openYesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel);
		} else {
			s2c(new StartYesNoVotingPayload(extraData, title, subtitle, yesLabel, noLabel));
		}
	}

	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().openNumberVotingScreen(extraData, title, subtitle, max, unavailable);
		} else {
			s2c(new StartNumberVotingPayload(extraData, title, subtitle, max, unavailable));
		}
	}

	default void removeAllParticles() {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().removeAllParticles();
		} else {
			s2c(RemoveAllParticlesPayload.INSTANCE);
		}
	}

	default void redrawSections(LongList sections, boolean mainThread) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().redrawSections(sections, mainThread);
		} else {
			s2c(new RedrawChunkSectionsPayload(sections, mainThread));
		}
	}

	default void playGlobalSound(PositionedSoundData data, WorldNumberVariables variables) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().playGlobalSound(data, variables);
		} else {
			s2c(new SoundPayload(data, variables));
		}
	}

	default void playGlobalSound(Vec3 pos, SoundData sound) {
		playGlobalSound(new PositionedSoundData(sound, WorldPosition.fixed(pos), false, false), WorldNumberVariables.EMPTY);
	}

	default void playGlobalSound(SoundData sound) {
		playGlobalSound(new PositionedSoundData(sound), WorldNumberVariables.EMPTY);
	}

	default void physicsParticles(PhysicsParticleData data, long seed, List<PositionedBlock> blocks) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().physicsParticles(data, seed, blocks);
		} else if (!blocks.isEmpty()) {
			s2c(new PhysicsParticlesPayload(data, seed, blocks));
		}
	}

	default void physicsParticles(ResourceLocation id, long seed, List<PositionedBlock> blocks) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().physicsParticles(id, seed, blocks);
		} else if (!blocks.isEmpty()) {
			s2c(new PhysicsParticlesIdPayload(id, seed, blocks));
		}
	}

	default void physicsParticles(PhysicsParticleData data, List<PositionedBlock> blocks) {
		physicsParticles(data, shimmer$level().shimmer$level().random.nextLong(), blocks);
	}

	default void physicsParticles(ResourceLocation id, List<PositionedBlock> blocks) {
		physicsParticles(id, shimmer$level().shimmer$level().random.nextLong(), blocks);
	}

	default void cubeParticles(Map<CubeParticleOptions, List<BlockPos>> map) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().cubeParticles(map);
		} else {
			s2c(new SpawnCubeParticlesPayload(map));
		}
	}

	default void cubeParticles(CubeParticleOptions options, List<BlockPos> blocks) {
		cubeParticles(Map.of(options, blocks));
	}

	default void textParticles(TextParticleOptions options, List<Vec3> positions) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().textParticles(options, positions);
		} else {
			s2c(new SpawnTextParticlePayload(options, positions));
		}
	}

	default void windParticles(RandomSource random, WindData data) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().windParticles(random, data);
		} else {
			s2c(new SpawnWindParticlesPayload(data));
		}
	}

	default void fireParticles(RandomSource random, FireData data) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().fireParticles(random, data);
		} else {
			s2c(new SpawnFireParticlesPayload(data));
		}
	}

	default void setScreenFade(Fade fade) {
		if (shimmer$isClient()) {
			shimmer$getEnvironment().setScreenFade(fade);
		} else {
			s2c(new ScreenFadePayload(fade));
		}
	}
}
