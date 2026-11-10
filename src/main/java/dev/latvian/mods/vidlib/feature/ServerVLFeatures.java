package dev.latvian.mods.vidlib.feature;

import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.particle.ShapeParticleOptions;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.vidlib.feature.bulk.RedrawChunkSectionsPayload;
import dev.latvian.mods.vidlib.feature.camera.ScreenShake;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeAtPositionPayload;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakePayload;
import dev.latvian.mods.vidlib.feature.camera.SetCameraModePayload;
import dev.latvian.mods.vidlib.feature.camera.StopScreenShakePayload;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.cutscene.PlayCutscenePayload;
import dev.latvian.mods.vidlib.feature.cutscene.StopCutscenePayload;
import dev.latvian.mods.vidlib.feature.hud.ToastDisplayPayload;
import dev.latvian.mods.vidlib.feature.misc.CloseScreenPayload;
import dev.latvian.mods.vidlib.feature.misc.InfoBarOverridePayload;
import dev.latvian.mods.vidlib.feature.misc.ReplayMarkerPayload;
import dev.latvian.mods.vidlib.feature.particle.RemoveAllParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.SpawnShapeParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesDisplayData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesPayload;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.Fade;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.ScreenFadePayload;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundPayload;
import dev.latvian.mods.vidlib.feature.vote.StartNumberVotingPayload;
import dev.latvian.mods.vidlib.feature.vote.StartYesNoVotingPayload;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public interface ServerVLFeatures extends VLFeatures {
	record Instance(PlayerTarget target) implements ServerVLFeatures {
	}

	PlayerTarget target();

	@Override
	default void toast(String uniqueId, long displayTime, Component title, Component description) {
		target().s2c(new ToastDisplayPayload(uniqueId, displayTime, title, description));
	}

	@Override
	default void playCutscene(Ref<Cutscene> cutscene, KNumberVariables variables) {
		if (!cutscene.value().steps().isEmpty()) {
			target().s2c(new PlayCutscenePayload(cutscene, variables));
		}
	}

	@Override
	default void stopCutscene() {
		target().s2c(StopCutscenePayload.INSTANCE);
	}

	@Override
	default void screenShake(ScreenShake shake) {
		if (!shake.skip()) {
			target().s2c(new ScreenShakePayload(shake));
		}
	}

	@Override
	default void screenShake(ScreenShake shake, Vec3 source, double maxDistance) {
		target().s2c(new ScreenShakeAtPositionPayload(shake, source, maxDistance));
	}

	@Override
	default void stopScreenShake() {
		target().s2c(StopScreenShakePayload.INSTANCE);
	}

	@Override
	default void setCameraMode(int mode) {
		target().s2c(new SetCameraModePayload(mode));
	}

	@Override
	default void closeScreen() {
		target().s2c(CloseScreenPayload.INSTANCE);
	}

	@Override
	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel, boolean closeOnVote) {
		target().s2c(new StartYesNoVotingPayload(extraData, title, subtitle, yesLabel, noLabel, closeOnVote));
	}

	@Override
	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable, boolean closeOnVote) {
		target().s2c(new StartNumberVotingPayload(extraData, title, subtitle, max, unavailable, closeOnVote));
	}

	@Override
	default void removeAllParticles() {
		target().s2c(RemoveAllParticlesPayload.INSTANCE);
	}

	@Override
	default void redrawSections(LongList sections, boolean mainThread) {
		target().s2c(new RedrawChunkSectionsPayload(sections, mainThread));
	}

	@Override
	default void playGlobalSound(PositionedSoundData data, KNumberVariables variables) {
		target().s2c(new SoundPayload(data, variables));
	}

	@Override
	default void physicsParticles(PhysicsParticlesDisplayData data, long spawnTime) {
		target().s2c(new PhysicsParticlesPayload(data, spawnTime));
	}

	@Override
	default void shapeParticles(Map<ShapeParticleOptions, List<BlockPos>> map) {
		target().s2c(new SpawnShapeParticlesPayload(map));
	}

	@Override
	default void setScreenFade(Fade fade) {
		target().s2c(new ScreenFadePayload(fade));
	}

	@Override
	default void marker(ReplayMarkerData data) {
		target().s2c(new ReplayMarkerPayload(data));
	}

	@Override
	default void setInfoBarText(int bar, Component text) {
		target().s2c(new InfoBarOverridePayload(bar, text));
	}
}
