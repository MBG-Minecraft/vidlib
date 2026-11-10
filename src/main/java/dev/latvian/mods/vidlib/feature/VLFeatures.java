package dev.latvian.mods.vidlib.feature;

import dev.latvian.mods.klib.block.PositionedBlock;
import dev.latvian.mods.klib.block.collection.PositionedBlockCollection;
import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.particle.ShapeParticleOptions;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.vidlib.feature.camera.ScreenShake;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.note.Note;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesDisplayData;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.Fade;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VLFeatures {
	static VLFeatures of(PlayerTarget target) {
		if (target.getCommonLevel().isClientSide()) {
			return (VLFeatures) LevelSessionData.of(target.getCommonLevel()).globalData;
		} else {
			return new ServerVLFeatures.Instance(target);
		}
	}

	static VLFeatures of(MinecraftServer server) {
		return of(PlayerTarget.of(server));
	}

	static VLFeatures of(Level level) {
		return of(PlayerTarget.of(level));
	}

	static VLFeatures of(Player player) {
		return of(PlayerTarget.of(player));
	}

	default void updateInput(Level level, UUID player, PlayerInput input) {
	}

	default void toast(Component title, Component description) {
		toast("", 5000L, title, description);
	}

	default void updateAtmospheres() {
	}

	default void createNote(Note note) {
	}

	default void deleteNote(UUID id) {
	}

	default void setEntityBrightnessOverride(int entityId, int override) {
	}

	default void toast(String uniqueId, long displayTime, Component title, Component description) {
	}

	default void playCutscene(Ref<Cutscene> cutscene, KNumberVariables variables) {
	}

	default void stopCutscene() {
	}

	default void screenShake(ScreenShake shake) {
	}

	default void screenShake(ScreenShake shake, Vec3 source, double maxDistance) {
	}

	default void stopScreenShake() {
	}

	default void setCameraMode(int mode) {
	}

	default void setPostEffect(Identifier id) {
	}

	default void closeScreen() {
	}

	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel, boolean closeOnVote) {
	}

	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable, boolean closeOnVote) {
	}

	default void removeAllParticles() {
	}

	default void redrawSections(LongList sections, boolean mainThread) {
	}

	default void playGlobalSound(PositionedSoundData data, KNumberVariables variables) {
	}

	default void playGlobalSound(Vec3 pos, SoundData sound) {
		playGlobalSound(new PositionedSoundData(sound, KVector.of(pos).ref(), false, false), KNumberVariables.EMPTY);
	}

	default void playGlobalSound(SoundData sound) {
		playGlobalSound(new PositionedSoundData(sound), KNumberVariables.EMPTY);
	}

	default void physicsParticles(PhysicsParticlesDisplayData data, long spawnTime) {
	}

	default void physicsParticles(LevelAccessor level, Ref<PhysicsParticleData> data, long seed, List<PositionedBlock> blocks) {
		if (!blocks.isEmpty()) {
			var displayData = new PhysicsParticlesDisplayData(data, seed, blocks.size() == 1 ? blocks.getFirst().ref() : new PositionedBlockCollection(blocks).ref());
			physicsParticles(displayData, level.getGameTime());
		}
	}

	default void physicsParticles(LevelAccessor level, Ref<PhysicsParticleData> data, List<PositionedBlock> blocks) {
		physicsParticles(level, data, level.getRandom().nextLong(), blocks);
	}

	default void shapeParticles(Map<ShapeParticleOptions, List<BlockPos>> map) {
	}

	default void shapeParticles(ShapeParticleOptions options, List<BlockPos> blocks) {
		shapeParticles(Map.of(options, blocks));
	}

	default void setScreenFade(Fade fade) {
	}

	default void marker(ReplayMarkerData data) {
	}

	default void setInfoBarText(int bar, Component text) {
	}
}
