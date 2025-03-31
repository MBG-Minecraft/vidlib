package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeInstance;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneScreen;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.data.UpdatePlayerDataValuePayload;
import dev.beast.mods.shimmer.feature.fade.Fade;
import dev.beast.mods.shimmer.feature.fade.ScreenFadeInstance;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.feature.particle.FireData;
import dev.beast.mods.shimmer.feature.particle.TextParticleOptions;
import dev.beast.mods.shimmer.feature.particle.WindData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleManager;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticles;
import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.sound.TrackingSound;
import dev.beast.mods.shimmer.feature.vote.NumberVotingScreen;
import dev.beast.mods.shimmer.feature.vote.YesNoVotingScreen;
import dev.beast.mods.shimmer.math.Vec2d;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import dev.beast.mods.shimmer.util.Empty;
import dev.beast.mods.shimmer.util.PauseType;
import dev.beast.mods.shimmer.util.ScheduledTask;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("resource")
public interface ShimmerMinecraftClient extends ShimmerMinecraftEnvironment {
	default Minecraft shimmer$self() {
		return (Minecraft) this;
	}

	@Override
	default boolean shimmer$isClient() {
		return true;
	}

	@Override
	default void c2s(@Nullable Packet<? super ServerGamePacketListener> packet) {
		if (packet != null) {
			shimmer$self().getConnection().send(packet);
		}
	}

	@Override
	default ClientLevel shimmer$level() {
		return shimmer$self().level;
	}

	@Override
	default ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		return shimmer$self().player.shimmer$sessionData().getScheduledTaskHandler();
	}

	@Override
	default DataMap getServerData() {
		return shimmer$self().player.shimmer$sessionData().serverDataMap;
	}

	@Override
	default PauseType getPauseType() {
		var mc = shimmer$self();
		return mc.isPaused() ? PauseType.GAME : mc.level != null && mc.level.tickRateManager().isFrozen() ? PauseType.TICK : PauseType.NONE;
	}

	@ApiStatus.Internal
	default void shimmer$renderSetup(RenderLevelStageEvent event, float delta) {
		var player = shimmer$self().player;

		if (player == null) {
			return;
		}

		var session = player.shimmer$sessionData();

		var ray = shimmer$self().gameRenderer.getMainCamera().ray(512D);

		if (shimmer$self().options.getCameraType() == CameraType.FIRST_PERSON && player.getShowZones()) {
			session.zoneClip = session.filteredZones.clip(ray);
		} else {
			session.zoneClip = null;
		}
	}

	default Vec2d shimmer$getCameraShakeOffset(float delta) {
		var player = shimmer$self().player;

		if (player != null) {
			var s = player.shimmer$sessionData();
			return s.prevCameraShake.lerp(s.cameraShake, delta);
		}

		return Vec2d.ZERO;
	}

	default void shimmer$applyCameraShake(Camera camera, float delta) {
		var shake = shimmer$getCameraShakeOffset(delta);

		if (shake != Vec2d.ZERO) {
			var vec = new Vector4f((float) shake.x, (float) shake.y, 0F, 1F).rotate(camera.rotation());
			camera.shimmer$setPosition(camera.getPosition().add(vec.x(), vec.y(), vec.z()));
		}
	}

	default void updatePlayerData(List<DataMapValue> update) {
		c2s(new UpdatePlayerDataValuePayload(update));
	}

	default <T> void updatePlayerData(DataType<T> type, T value) {
		updatePlayerData(List.of(new DataMapValue(type, value)));
	}

	@Override
	default void shimmer$preTick(PauseType paused) {
		var level = shimmer$self().level;
		var player = shimmer$self().player;

		if (level == null || player == null) {
			return;
		}

		player.shimmer$sessionData().preTick(level, player, shimmer$self().getWindow(), paused);
	}

	@Override
	default void shimmer$postTick(PauseType paused) {
		var level = shimmer$self().level;
		var player = shimmer$self().player;

		if (player != null && level != null) {
			player.shimmer$sessionData().postTick(level, player, paused);
		}

		if (level != null && paused.tick()) {
			PhysicsParticleManager.tickAll(level, level.getGameTime());
		}
	}

	@Override
	default void tell(Component message) {
		shimmer$self().player.displayClientMessage(message, false);
	}

	@Override
	default void status(Component message) {
		shimmer$self().player.displayClientMessage(message, true);
	}

	@Override
	default void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		var level = shimmer$self().level;
		var player = shimmer$self().player;

		if (!cutscene.steps.isEmpty() && player != null) {
			var ctx = new WorldNumberContext(level, 0F, variables);

			for (var step : cutscene.steps) {
				step.resolvedStart = (int) step.start.get(ctx);
				step.resolvedLength = (int) step.length.get(ctx);
			}

			var overrideCamera = !player.isReplayCamera();
			var inst = new ClientCutscene(shimmer$self(), overrideCamera, cutscene, variables, player::getEyePosition);
			player.shimmer$sessionData().cutscene = inst;

			if (overrideCamera && !cutscene.allowMovement) {
				shimmer$self().setScreen(new CutsceneScreen(inst, shimmer$self().screen));
			}

			shimmer$self().options.hideGui = true;
		}
	}

	@Override
	default void stopCutscene() {
		shimmer$self().player.shimmer$sessionData().cutscene = null;

		if (shimmer$self().screen instanceof CutsceneScreen screen) {
			shimmer$self().setScreen(screen.previousScreen);
		}

		shimmer$self().options.hideGui = false;
		shimmer$self().gameRenderer.clearPostEffect();
	}

	@Override
	default void shakeCamera(CameraShake shake) {
		if (shake.skip()) {
			return;
		}

		shimmer$self().player.shimmer$sessionData().cameraShakeInstances.add(new CameraShakeInstance(shake));

		if (shake.motionBlur()) {
			shimmer$self().gameRenderer.setPostEffect(CameraShake.MOTION_BLUR_EFFECT);
		}
	}

	@Override
	default void shakeCamera(CameraShake shake, Vec3 source, double maxDistance) {
		shakeCamera(shake.atDistance(shimmer$self().gameRenderer.getMainCamera().getPosition(), source, maxDistance));
	}

	@Override
	default void stopCameraShaking() {
		shimmer$self().player.shimmer$sessionData().cameraShakeInstances.clear();
	}

	@Override
	default void setPostEffect(ResourceLocation id) {
		if (id.equals(Empty.ID)) {
			shimmer$self().gameRenderer.clearPostEffect();
		} else {
			shimmer$self().gameRenderer.setPostEffect(id);
		}
	}

	@Override
	default void shimmer$closeScreen() {
		shimmer$self().popGuiLayer();
	}

	@Override
	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		shimmer$self().setScreen(new YesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel));
	}

	@Override
	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable) {
		shimmer$self().setScreen(new NumberVotingScreen(extraData, title, subtitle, max, unavailable));
	}

	@Override
	default void removeAllParticles() {
		shimmer$self().particleEngine.setLevel(shimmer$self().level);

		for (var manager : PhysicsParticleManager.ALL) {
			manager.clear();
		}
	}

	@Override
	default void removeZone(UUID uuid) {
		var session = shimmer$self().player.shimmer$sessionData();
		session.serverZones.remove(uuid);
		session.filteredZones.remove(uuid);
	}

	@Override
	default void redrawSections(LongList sections, boolean mainThread) {
		var levelRenderer = shimmer$self().levelRenderer;

		for (long section : sections) {
			int x = SectionPos.x(section);
			int y = SectionPos.y(section);
			int z = SectionPos.z(section);
			levelRenderer.setSectionDirty(x, y, z, mainThread);
		}
	}

	@Override
	default void playSound(Vec3 pos, SoundData sound) {
		var mc = shimmer$self();
		mc.getSoundManager().play(new SimpleSoundInstance(sound.sound().value(), sound.source(), sound.volume(), sound.pitch(), shimmer$level().random, pos.x, pos.y, pos.z));
	}

	@Override
	default void playTrackingSound(WorldPosition position, WorldNumberVariables variables, SoundData data, boolean looping) {
		shimmer$self().getSoundManager().play(new TrackingSound(shimmer$level(), position, variables, data, looping));
	}

	@Override
	default void physicsParticles(PhysicsParticleData data, long seed, List<PositionedBlock> blocks) {
		if (blocks.isEmpty()) {
			return;
		}

		var particles = new PhysicsParticles(data, shimmer$level(), shimmer$level().getGameTime(), seed);

		for (var block : blocks) {
			particles.at = block.pos();
			particles.state = block.state();
			particles.spawn();
		}
	}

	@Override
	default void physicsParticles(ResourceLocation id, long seed, List<PositionedBlock> blocks) {
		if (blocks.isEmpty()) {
			return;
		}

		var data = PhysicsParticleData.REGISTRY.get(id);
		physicsParticles(data == null ? PhysicsParticleData.DEFAULT : data, seed, blocks);
	}

	@Override
	default void cubeParticles(Map<CubeParticleOptions, List<BlockPos>> map) {
		for (var entry : map.entrySet()) {
			for (var pos : entry.getValue()) {
				shimmer$level().addParticle(entry.getKey(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
			}
		}
	}

	@Override
	default void textParticles(TextParticleOptions options, List<Vec3> positions) {
		for (var pos : positions) {
			shimmer$level().addParticle(options, pos.x, pos.y, pos.z, 0D, 0D, 0D);
		}
	}

	@Override
	default void windParticles(RandomSource random, WindData data) {
		var particles = shimmer$self().particleEngine;

		for (int i = 0; i < data.data().count(); i++) {
			var x = data.data().position().getX() + random.nextFloat();
			var y = data.data().position().getY() + random.nextFloat() * (data.options().ground() ? 0.12D : 1D);
			var z = data.data().position().getZ() + random.nextFloat();
			var v = data.data().delta(random);
			var p = particles.createParticle(data.options(), x, y, z, v.x(), v.y(), v.z());

			if (p != null) {
				particles.add(p);
			}
		}
	}

	@Override
	default void fireParticles(RandomSource random, FireData data) {
		var particles = shimmer$self().particleEngine;
		var options = data.options().withResolvedGradient();

		for (int i = 0; i < data.data().count(); i++) {
			var x = data.data().position().getX() + random.nextFloat();
			var y = data.data().position().getY() + random.nextFloat();
			var z = data.data().position().getZ() + random.nextFloat();
			var v = data.data().delta(random);
			var p = particles.createParticle(options, x, y, z, v.x(), v.y(), v.z());

			if (p != null) {
				particles.add(p);
			}
		}
	}

	@Override
	default void setScreenFade(Fade fade) {
		shimmer$self().player.shimmer$sessionData().screenFade = new ScreenFadeInstance(fade);
	}
}
