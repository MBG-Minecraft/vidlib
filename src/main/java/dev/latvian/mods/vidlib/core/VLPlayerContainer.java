package dev.latvian.mods.vidlib.core;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.bulk.RedrawChunkSectionsPayload;
import dev.latvian.mods.vidlib.feature.camera.CameraShake;
import dev.latvian.mods.vidlib.feature.camera.SetCameraModePayload;
import dev.latvian.mods.vidlib.feature.camera.ShakeCameraAtPositionPayload;
import dev.latvian.mods.vidlib.feature.camera.ShakeCameraPayload;
import dev.latvian.mods.vidlib.feature.camera.StopCameraShakingPayload;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.cutscene.PlayCutscenePayload;
import dev.latvian.mods.vidlib.feature.cutscene.StopCutscenePayload;
import dev.latvian.mods.vidlib.feature.fade.Fade;
import dev.latvian.mods.vidlib.feature.fade.ScreenFadePayload;
import dev.latvian.mods.vidlib.feature.highlight.TerrainHighlight;
import dev.latvian.mods.vidlib.feature.highlight.TerrainHighlightPayload;
import dev.latvian.mods.vidlib.feature.misc.CloseScreenPayload;
import dev.latvian.mods.vidlib.feature.misc.MarkerData;
import dev.latvian.mods.vidlib.feature.misc.MarkerPayload;
import dev.latvian.mods.vidlib.feature.misc.SetPostEffectPayload;
import dev.latvian.mods.vidlib.feature.particle.CubeParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.FireData;
import dev.latvian.mods.vidlib.feature.particle.ItemParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.LineParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.RemoveAllParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.SpawnCubeParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.SpawnFireParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.SpawnItemParticlePayload;
import dev.latvian.mods.vidlib.feature.particle.SpawnLineParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.SpawnTextParticlePayload;
import dev.latvian.mods.vidlib.feature.particle.SpawnWindParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.TextParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.WindData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesIdData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesIdPayload;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesPayload;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundPayload;
import dev.latvian.mods.vidlib.feature.vote.StartNumberVotingPayload;
import dev.latvian.mods.vidlib.feature.vote.StartYesNoVotingPayload;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import dev.latvian.mods.vidlib.util.MessageConsumer;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface VLPlayerContainer extends VLS2CPacketConsumer, VLC2SPacketConsumer, MessageConsumer {
	default List<? extends Player> vl$getS2CPlayers() {
		return List.of();
	}

	@Override
	default void s2c(@Nullable Packet<? super ClientGamePacketListener> packet) {
		for (var player : vl$getS2CPlayers()) {
			player.s2c(packet);
		}
	}

	@Override
	default void c2s(@Nullable Packet<? super ServerGamePacketListener> packet) {
		getEnvironment().c2s(packet);
	}

	@Override
	default void tell(Component message) {
		if (isClient()) {
			getEnvironment().tell(message);
		} else {
			for (var player : vl$getS2CPlayers()) {
				player.displayClientMessage(message, false);
			}
		}
	}

	@Override
	default void status(Component message) {
		if (isClient()) {
			getEnvironment().status(message);
		} else {
			for (var player : vl$getS2CPlayers()) {
				player.displayClientMessage(message, true);
			}
		}
	}

	default void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		if (isClient()) {
			getEnvironment().playCutscene(cutscene, variables);
		} else if (!cutscene.steps.isEmpty()) {
			s2c(new PlayCutscenePayload(cutscene, variables));
		}
	}

	default void stopCutscene() {
		if (isClient()) {
			getEnvironment().stopCutscene();
		} else {
			s2c(StopCutscenePayload.INSTANCE);
		}
	}

	default void shakeCamera(CameraShake shake) {
		if (isClient()) {
			getEnvironment().shakeCamera(shake);
		} else if (!shake.skip()) {
			s2c(new ShakeCameraPayload(shake));
		}
	}

	default void shakeCamera(CameraShake shake, Vec3 source, double maxDistance) {
		if (isClient()) {
			getEnvironment().shakeCamera(shake, source, maxDistance);
		} else {
			s2c(new ShakeCameraAtPositionPayload(shake, source, maxDistance));
		}
	}

	default void stopCameraShaking() {
		if (isClient()) {
			getEnvironment().stopCameraShaking();
		} else {
			s2c(StopCameraShakingPayload.INSTANCE);
		}
	}

	default void setCameraMode(int mode) {
		if (isClient()) {
			getEnvironment().setCameraMode(mode);
		} else {
			s2c(new SetCameraModePayload(mode));
		}
	}

	default void setPostEffect(ResourceLocation id) {
		if (isClient()) {
			getEnvironment().setPostEffect(id);
		} else {
			s2c(new SetPostEffectPayload(id));
		}
	}

	default void vl$closeScreen() {
		if (isClient()) {
			getEnvironment().vl$closeScreen();
		} else {
			s2c(CloseScreenPayload.INSTANCE);
		}
	}

	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		if (isClient()) {
			getEnvironment().openYesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel);
		} else {
			s2c(new StartYesNoVotingPayload(extraData, title, subtitle, yesLabel, noLabel));
		}
	}

	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable) {
		if (isClient()) {
			getEnvironment().openNumberVotingScreen(extraData, title, subtitle, max, unavailable);
		} else {
			s2c(new StartNumberVotingPayload(extraData, title, subtitle, max, unavailable));
		}
	}

	default void removeAllParticles() {
		if (isClient()) {
			getEnvironment().removeAllParticles();
		} else {
			s2c(RemoveAllParticlesPayload.INSTANCE);
		}
	}

	default void redrawSections(LongList sections, boolean mainThread) {
		if (isClient()) {
			getEnvironment().redrawSections(sections, mainThread);
		} else {
			s2c(new RedrawChunkSectionsPayload(sections, mainThread));
		}
	}

	default void playGlobalSound(PositionedSoundData data, WorldNumberVariables variables) {
		if (isClient()) {
			getEnvironment().playGlobalSound(data, variables);
		} else {
			s2c(new SoundPayload(data, variables));
		}
	}

	default void playGlobalSound(Vec3 pos, SoundData sound) {
		playGlobalSound(new PositionedSoundData(sound, WorldVector.fixed(pos), false, false), WorldNumberVariables.EMPTY);
	}

	default void playGlobalSound(SoundData sound) {
		playGlobalSound(new PositionedSoundData(sound), WorldNumberVariables.EMPTY);
	}

	default void physicsParticles(PhysicsParticleData data, long spawnTime, long seed, List<PositionedBlock> blocks) {
		if (isClient()) {
			getEnvironment().physicsParticles(data, spawnTime, seed, blocks);
		} else if (!blocks.isEmpty()) {
			s2c(new PhysicsParticlesPayload(data, seed, blocks));
		}
	}

	default void physicsParticles(PhysicsParticlesIdData data, long spawnTime) {
		if (isClient()) {
			getEnvironment().physicsParticles(data, spawnTime);
		} else if (!data.blocks().isEmpty()) {
			s2c(new PhysicsParticlesIdPayload(data));
		}
	}

	default void physicsParticles(ResourceLocation id, long seed, List<PositionedBlock> blocks) {
		physicsParticles(new PhysicsParticlesIdData(id, seed, blocks), vl$level().getGameTime());
	}

	default void physicsParticles(PhysicsParticleData data, List<PositionedBlock> blocks) {
		physicsParticles(data, vl$level().getGameTime(), vl$level().vl$level().random.nextLong(), blocks);
	}

	default void physicsParticles(ResourceLocation id, List<PositionedBlock> blocks) {
		physicsParticles(id, vl$level().vl$level().random.nextLong(), blocks);
	}

	default void cubeParticles(Map<CubeParticleOptions, List<BlockPos>> map) {
		if (isClient()) {
			getEnvironment().cubeParticles(map);
		} else {
			s2c(new SpawnCubeParticlesPayload(map));
		}
	}

	default void cubeParticles(CubeParticleOptions options, List<BlockPos> blocks) {
		cubeParticles(Map.of(options, blocks));
	}

	default void lineParticles(Map<LineParticleOptions, List<AABB>> map) {
		if (isClient()) {
			getEnvironment().lineParticles(map);
		} else {
			s2c(new SpawnLineParticlesPayload(map));
		}
	}

	default void lineParticles(LineParticleOptions options, List<AABB> blocks) {
		lineParticles(Map.of(options, blocks));
	}

	default void textParticles(TextParticleOptions options, List<Vec3> positions) {
		if (isClient()) {
			getEnvironment().textParticles(options, positions);
		} else {
			s2c(new SpawnTextParticlePayload(options, positions));
		}
	}

	default void itemParticles(ItemParticleOptions options, List<Pair<Vec3, Vec3>> positions) {
		if (isClient()) {
			getEnvironment().itemParticles(options, positions);
		} else {
			s2c(new SpawnItemParticlePayload(options, positions));
		}
	}

	default void itemParticles(ItemParticleOptions options, Vec3 pos, Vec3 vel) {
		itemParticles(options, List.of(Pair.of(pos, vel)));
	}

	default void windParticles(RandomSource random, WindData data) {
		if (isClient()) {
			getEnvironment().windParticles(random, data);
		} else {
			s2c(new SpawnWindParticlesPayload(data));
		}
	}

	default void fireParticles(RandomSource random, FireData data) {
		if (isClient()) {
			getEnvironment().fireParticles(random, data);
		} else {
			s2c(new SpawnFireParticlesPayload(data));
		}
	}

	default void setScreenFade(Fade fade) {
		if (isClient()) {
			getEnvironment().setScreenFade(fade);
		} else {
			s2c(new ScreenFadePayload(fade));
		}
	}

	default void marker(MarkerData data) {
		if (isClient()) {
			getEnvironment().marker(data);
		} else {
			s2c(new MarkerPayload(data));
		}
	}

	default void addTerrainHighlight(TerrainHighlight highlight) {
		if (isClient()) {
			getEnvironment().addTerrainHighlight(highlight);
		} else {
			s2c(new TerrainHighlightPayload(highlight));
		}
	}
}
