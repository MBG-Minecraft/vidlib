package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.block.PositionedBlock;
import dev.latvian.mods.klib.block.collection.PositionedBlockCollection;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.MessageConsumer;
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
import dev.latvian.mods.vidlib.feature.misc.SetPostEffectPayload;
import dev.latvian.mods.vidlib.feature.particle.RemoveAllParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.ShapeParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.SpawnCubeParticlesPayload;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesDisplayData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesPayload;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.Fade;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.ScreenFadePayload;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundPayload;
import dev.latvian.mods.vidlib.feature.vote.StartNumberVotingPayload;
import dev.latvian.mods.vidlib.feature.vote.StartYesNoVotingPayload;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface VLPlayerContainer extends VLLevelContainer, VLS2CPacketConsumer, VLC2SPacketConsumer, MessageConsumer {
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
		if (isClientEnvironment()) {
			getEnvironment().tell(message);
		} else {
			for (var player : vl$getS2CPlayers()) {
				player.sendSystemMessage(message);
			}
		}
	}

	@Override
	default void status(Component message) {
		if (isClientEnvironment()) {
			getEnvironment().status(message);
		} else {
			for (var player : vl$getS2CPlayers()) {
				player.sendOverlayMessage(message);
			}
		}
	}

	default void toast(String uniqueId, long displayTime, Component title, Component description) {
		if (isClientEnvironment()) {
			getEnvironment().toast(uniqueId, displayTime, title, description);
		} else {
			s2c(new ToastDisplayPayload(uniqueId, displayTime, title, description));
		}
	}

	default void toast(Component title, Component description) {
		toast("", 5000L, title, description);
	}

	default void playCutscene(Ref<Cutscene> cutscene, KNumberVariables variables) {
		if (isClientEnvironment()) {
			getEnvironment().playCutscene(cutscene, variables);
		} else if (!cutscene.value().steps().isEmpty()) {
			s2c(new PlayCutscenePayload(cutscene, variables));
		}
	}

	default void stopCutscene() {
		if (isClientEnvironment()) {
			getEnvironment().stopCutscene();
		} else {
			s2c(StopCutscenePayload.INSTANCE);
		}
	}

	default void screenShake(ScreenShake shake) {
		if (isClientEnvironment()) {
			getEnvironment().screenShake(shake);
		} else if (!shake.skip()) {
			s2c(new ScreenShakePayload(shake));
		}
	}

	default void screenShake(ScreenShake shake, Vec3 source, double maxDistance) {
		if (isClientEnvironment()) {
			getEnvironment().screenShake(shake, source, maxDistance);
		} else {
			s2c(new ScreenShakeAtPositionPayload(shake, source, maxDistance));
		}
	}

	default void stopScreenShake() {
		if (isClientEnvironment()) {
			getEnvironment().stopScreenShake();
		} else {
			s2c(StopScreenShakePayload.INSTANCE);
		}
	}

	default void setCameraMode(int mode) {
		if (isClientEnvironment()) {
			getEnvironment().setCameraMode(mode);
		} else {
			s2c(new SetCameraModePayload(mode));
		}
	}

	default void setPostEffect(Identifier id) {
		if (isClientEnvironment()) {
			getEnvironment().setPostEffect(id);
		} else {
			s2c(new SetPostEffectPayload(id));
		}
	}

	default void vl$closeScreen() {
		if (isClientEnvironment()) {
			getEnvironment().vl$closeScreen();
		} else {
			s2c(CloseScreenPayload.INSTANCE);
		}
	}

	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel, boolean closeOnVote) {
		if (isClientEnvironment()) {
			getEnvironment().openYesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel, closeOnVote);
		} else {
			s2c(new StartYesNoVotingPayload(extraData, title, subtitle, yesLabel, noLabel, closeOnVote));
		}
	}

	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable, boolean closeOnVote) {
		if (isClientEnvironment()) {
			getEnvironment().openNumberVotingScreen(extraData, title, subtitle, max, unavailable, closeOnVote);
		} else {
			s2c(new StartNumberVotingPayload(extraData, title, subtitle, max, unavailable, closeOnVote));
		}
	}

	default void removeAllParticles() {
		if (isClientEnvironment()) {
			getEnvironment().removeAllParticles();
		} else {
			s2c(RemoveAllParticlesPayload.INSTANCE);
		}
	}

	default void redrawSections(LongList sections, boolean mainThread) {
		if (isClientEnvironment()) {
			getEnvironment().redrawSections(sections, mainThread);
		} else {
			s2c(new RedrawChunkSectionsPayload(sections, mainThread));
		}
	}

	default void playGlobalSound(PositionedSoundData data, KNumberVariables variables) {
		if (isClientEnvironment()) {
			getEnvironment().playGlobalSound(data, variables);
		} else {
			s2c(new SoundPayload(data, variables));
		}
	}

	default void playGlobalSound(Vec3 pos, SoundData sound) {
		playGlobalSound(new PositionedSoundData(sound, KVector.of(pos).ref(), false, false), KNumberVariables.EMPTY);
	}

	default void playGlobalSound(SoundData sound) {
		playGlobalSound(new PositionedSoundData(sound), KNumberVariables.EMPTY);
	}

	default void physicsParticles(PhysicsParticlesDisplayData data, long spawnTime) {
		if (isClientEnvironment()) {
			getEnvironment().physicsParticles(data, spawnTime);
		} else {
			s2c(new PhysicsParticlesPayload(data, spawnTime));
		}
	}

	default void physicsParticles(Ref<PhysicsParticleData> data, long seed, List<PositionedBlock> blocks) {
		if (!blocks.isEmpty()) {
			physicsParticles(new PhysicsParticlesDisplayData(data, seed, blocks.size() == 1 ? blocks.getFirst().ref() : new PositionedBlockCollection(blocks).ref()), vl$level().getGameTime());
		}
	}

	default void physicsParticles(Ref<PhysicsParticleData> data, List<PositionedBlock> blocks) {
		physicsParticles(data, vl$level().vl$level().getRandom().nextLong(), blocks);
	}

	default void cubeParticles(Map<ShapeParticleOptions, List<BlockPos>> map) {
		if (isClientEnvironment()) {
			getEnvironment().cubeParticles(map);
		} else {
			s2c(new SpawnCubeParticlesPayload(map));
		}
	}

	default void cubeParticles(ShapeParticleOptions options, List<BlockPos> blocks) {
		cubeParticles(Map.of(options, blocks));
	}

	default void setScreenFade(Fade fade) {
		if (isClientEnvironment()) {
			getEnvironment().setScreenFade(fade);
		} else {
			s2c(new ScreenFadePayload(fade));
		}
	}

	default void marker(ReplayMarkerData data) {
		if (isClientEnvironment()) {
			getEnvironment().marker(data);
		} else {
			s2c(new ReplayMarkerPayload(data));
		}
	}

	default void setInfoBarText(int bar, Component text) {
		if (isClientEnvironment()) {
			getEnvironment().setInfoBarText(bar, text);
		} else {
			s2c(new InfoBarOverridePayload(bar, text));
		}
	}
}
