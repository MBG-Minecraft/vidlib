package dev.beast.mods.shimmer.core;

import com.mojang.datafixers.util.Pair;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.camera.CameraShake;
import dev.beast.mods.shimmer.feature.camera.CameraShakeInstance;
import dev.beast.mods.shimmer.feature.camera.DetachedCamera;
import dev.beast.mods.shimmer.feature.camera.FreeCamera;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneScreen;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.data.UpdatePlayerDataValuePayload;
import dev.beast.mods.shimmer.feature.fade.Fade;
import dev.beast.mods.shimmer.feature.fade.ScreenFadeInstance;
import dev.beast.mods.shimmer.feature.misc.MarkerData;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.feature.particle.FireData;
import dev.beast.mods.shimmer.feature.particle.ItemParticleOptions;
import dev.beast.mods.shimmer.feature.particle.LineParticleOptions;
import dev.beast.mods.shimmer.feature.particle.TextParticleOptions;
import dev.beast.mods.shimmer.feature.particle.WindData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleManager;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticles;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticlesIdData;
import dev.beast.mods.shimmer.feature.sound.PositionedSoundData;
import dev.beast.mods.shimmer.feature.sound.ShimmerSoundInstance;
import dev.beast.mods.shimmer.feature.vote.NumberVotingScreen;
import dev.beast.mods.shimmer.feature.vote.YesNoVotingScreen;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.util.Empty;
import dev.beast.mods.shimmer.util.FrameInfo;
import dev.beast.mods.shimmer.util.PauseType;
import dev.beast.mods.shimmer.util.ScheduledTask;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.kmath.Vec2d;
import dev.latvian.mods.kmath.WorldMouse;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

	default WorldMouse getWorldMouse() {
		var session = shimmer$self().player.shimmer$sessionData();

		if (session.worldMouse == null) {
			session.worldMouse = WorldMouse.clip(shimmer$self(), session.currentFrameInfo.camera().getPosition(), session.currentFrameInfo.worldMatrix());
		}

		return session.worldMouse;
	}

	@ApiStatus.Internal
	default void shimmer$renderSetup(FrameInfo frame) {
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
			var vec = new Vector4f((float) shake.x(), (float) shake.y(), 0F, 1F).rotate(camera.rotation());
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
			player.shimmer$sessionData().cameraOverride = inst;

			if (overrideCamera && !cutscene.allowMovement) {
				shimmer$self().setScreen(new CutsceneScreen(inst, shimmer$self().screen));
			}

			shimmer$self().options.hideGui = true;
		}
	}

	@Override
	default void stopCutscene() {
		var data = shimmer$self().player.shimmer$sessionData();

		if (data.cameraOverride instanceof ClientCutscene cc) {
			cc.stopped();
		}

		data.cameraOverride = null;

		if (shimmer$self().screen instanceof CutsceneScreen screen) {
			shimmer$self().setScreen(screen.previousScreen);
		}

		shimmer$self().options.hideGui = false;
	}

	@Override
	default void shakeCamera(CameraShake shake) {
		if (shake.skip() || shimmer$self().player.isReplayCamera()) {
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
	default void setCameraMode(int mode) {
		var player = shimmer$self().player;

		var session = player.shimmer$sessionData();

		if (session.cameraOverride instanceof ClientCutscene) {
			stopCutscene();
		}

		switch (mode) {
			case 1 -> {
				if (session.cameraOverride instanceof FreeCamera c) {
					session.cameraOverride = new DetachedCamera(c.position, c.rotation);
				} else {
					session.cameraOverride = new DetachedCamera(player.getEyePosition(), Rotation.of(player, 1F));
				}
			}
			case 2 -> {
				if (session.cameraOverride instanceof DetachedCamera c) {
					session.cameraOverride = new FreeCamera(c.position(), c.rotation());
				} else {
					session.cameraOverride = new FreeCamera(player.getEyePosition(), Rotation.of(player, 1F));
				}
			}
			default -> stopCutscene();
		}
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
	default void playGlobalSound(PositionedSoundData data, WorldNumberVariables variables) {
		shimmer$self().getSoundManager().play(createGlobalSound(data, variables));
	}

	default SoundInstance createGlobalSound(PositionedSoundData data, WorldNumberVariables variables) {
		var mc = shimmer$self();

		if (data.position().isPresent()) {
			return new ShimmerSoundInstance(mc.level, data, variables);
		} else {
			return SimpleSoundInstance.forUI(data.data().sound().value(), data.data().pitch(), data.data().volume());
		}
	}

	@Override
	default void physicsParticles(PhysicsParticleData data, long spawnTime, long seed, List<PositionedBlock> blocks) {
		if (blocks.isEmpty()) {
			return;
		}

		var realTime = shimmer$level().getGameTime();

		if (spawnTime < realTime - 60L || spawnTime > realTime + 60L + (long) data.lifespan.max()) {
			Shimmer.LOGGER.info("Discarded physics particles packet @ " + realTime + " from " + spawnTime);
			return;
		}

		var particles = new PhysicsParticles(data, shimmer$level(), spawnTime, seed == 0L ? shimmer$self().level.getRandom().nextLong() : seed);

		for (var block : blocks) {
			particles.at = block.pos();
			particles.state = block.state();
			particles.spawn();
		}
	}

	@Override
	default void physicsParticles(PhysicsParticlesIdData data, long spawnTime) {
		if (!data.blocks().isEmpty()) {
			var p = PhysicsParticleData.REGISTRY.get(data.id());
			physicsParticles(p == null ? PhysicsParticleData.DEFAULT : p, spawnTime, data.seed(), data.blocks());
		}
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
	default void lineParticles(Map<LineParticleOptions, List<AABB>> map) {
		for (var entry : map.entrySet()) {
			for (var box : entry.getValue()) {
				shimmer$level().addParticle(entry.getKey(), box.minX, box.minY, box.minZ, box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ);
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
	default void itemParticles(ItemParticleOptions options, List<Pair<Vec3, Vec3>> positions) {
		for (var pair : positions) {
			var pos = pair.getFirst();
			var vel = pair.getSecond();
			shimmer$level().addParticle(options, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
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
		if (shimmer$self().player.isReplayCamera()) {
			return;
		}

		shimmer$self().player.shimmer$sessionData().screenFade = new ScreenFadeInstance(fade);
	}

	@Override
	default void marker(MarkerData data) {
		// Shimmer.LOGGER.info("Marker " + data.event() + "/" + data.name() + " (" + data.uuid() + ") @ ");
	}
}
