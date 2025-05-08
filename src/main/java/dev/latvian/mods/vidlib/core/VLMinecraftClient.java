package dev.latvian.mods.vidlib.core;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.kmath.Vec2d;
import dev.latvian.mods.kmath.WorldMouse;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.camera.CameraShake;
import dev.latvian.mods.vidlib.feature.camera.CameraShakeInstance;
import dev.latvian.mods.vidlib.feature.camera.DetachedCamera;
import dev.latvian.mods.vidlib.feature.camera.FreeCamera;
import dev.latvian.mods.vidlib.feature.cutscene.ClientCutscene;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneScreen;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.DataMapValue;
import dev.latvian.mods.vidlib.feature.data.DataType;
import dev.latvian.mods.vidlib.feature.data.UpdatePlayerDataValuePayload;
import dev.latvian.mods.vidlib.feature.fade.Fade;
import dev.latvian.mods.vidlib.feature.fade.ScreenFadeInstance;
import dev.latvian.mods.vidlib.feature.misc.MarkerData;
import dev.latvian.mods.vidlib.feature.particle.CubeParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.FireData;
import dev.latvian.mods.vidlib.feature.particle.ItemParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.LineParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.TextParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.WindData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesIdData;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.VidLibSoundInstance;
import dev.latvian.mods.vidlib.feature.vote.NumberVotingScreen;
import dev.latvian.mods.vidlib.feature.vote.YesNoVotingScreen;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;
import dev.latvian.mods.vidlib.util.Empty;
import dev.latvian.mods.vidlib.util.FrameInfo;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.latvian.mods.vidlib.util.PauseType;
import dev.latvian.mods.vidlib.util.ScheduledTask;
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
public interface VLMinecraftClient extends VLMinecraftEnvironment {
	default Minecraft vl$self() {
		return (Minecraft) this;
	}

	@Override
	default boolean isClient() {
		return true;
	}

	@Override
	default void c2s(@Nullable Packet<? super ServerGamePacketListener> packet) {
		if (packet != null) {
			vl$self().getConnection().send(packet);
		}
	}

	@Override
	default ClientLevel vl$level() {
		return vl$self().level;
	}

	@Override
	default ScheduledTask.Handler vl$getScheduledTaskHandler() {
		return vl$self().player.vl$sessionData().getScheduledTaskHandler();
	}

	@Override
	default DataMap getServerData() {
		return vl$self().player.vl$sessionData().serverDataMap;
	}

	@Override
	default PauseType getPauseType() {
		var mc = vl$self();
		return mc.isPaused() ? PauseType.GAME : mc.level != null && mc.level.tickRateManager().runsNormally() ? PauseType.NONE : PauseType.TICK;
	}

	default WorldMouse getWorldMouse() {
		var session = vl$self().player.vl$sessionData();

		if (session.worldMouse == null) {
			session.worldMouse = WorldMouse.clip(vl$self(), session.currentFrameInfo.camera().getPosition(), session.currentFrameInfo.worldMatrix());
		}

		return session.worldMouse;
	}

	@ApiStatus.Internal
	default void vl$renderSetup(FrameInfo frame) {
		var player = vl$self().player;

		if (player == null) {
			return;
		}

		var session = player.vl$sessionData();
		var ray = vl$self().gameRenderer.getMainCamera().ray(512D);

		if (vl$self().options.getCameraType() == CameraType.FIRST_PERSON && player.getShowZones()) {
			session.zoneClip = session.filteredZones.clip(ray);
		} else {
			session.zoneClip = null;
		}
	}

	default Vec2d vl$getCameraShakeOffset(float delta) {
		var player = vl$self().player;

		if (player != null) {
			var s = player.vl$sessionData();
			return s.prevCameraShake.lerp(s.cameraShake, delta);
		}

		return Vec2d.ZERO;
	}

	default void vl$applyCameraShake(Camera camera, float delta) {
		var shake = vl$getCameraShakeOffset(delta);

		if (shake != Vec2d.ZERO) {
			var vec = new Vector4f((float) shake.x(), (float) shake.y(), 0F, 1F).rotate(camera.rotation());
			camera.vl$setPosition(camera.getPosition().add(vec.x(), vec.y(), vec.z()));
		}
	}

	default void updatePlayerData(List<DataMapValue> update) {
		c2s(new UpdatePlayerDataValuePayload(update));
	}

	default <T> void updatePlayerData(DataType<T> type, T value) {
		updatePlayerData(List.of(new DataMapValue(type, value)));
	}

	@Override
	default void vl$preTick(PauseType paused) {
		var level = vl$self().level;
		var player = vl$self().player;

		if (level == null || player == null) {
			return;
		}

		player.vl$sessionData().preTick(level, player, vl$self().getWindow(), paused);
	}

	@Override
	default void vl$postTick(PauseType paused) {
		var level = vl$self().level;
		var player = vl$self().player;

		if (player != null && level != null) {
			player.vl$sessionData().postTick(level, player, paused);
		}

		if (level != null && paused.tick()) {
			PhysicsParticleManager.tickAll(level, level.getGameTime());
		}
	}

	@Override
	default void tell(Component message) {
		vl$self().player.displayClientMessage(message, false);
	}

	@Override
	default void status(Component message) {
		vl$self().player.displayClientMessage(message, true);
	}

	@Override
	default void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		var level = vl$self().level;
		var player = vl$self().player;

		if (!cutscene.steps.isEmpty() && player != null) {
			var ctx = new WorldNumberContext(level, 0F, variables);

			for (var step : cutscene.steps) {
				step.resolvedStart = (int) step.start.get(ctx);
				step.resolvedLength = (int) step.length.get(ctx);
			}

			var overrideCamera = !player.isReplayCamera();
			var inst = new ClientCutscene(vl$self(), overrideCamera, cutscene, variables, player::getEyePosition);
			player.vl$sessionData().cameraOverride = inst;

			if (overrideCamera && !cutscene.allowMovement) {
				vl$self().setScreen(new CutsceneScreen(inst, vl$self().screen));
			}

			vl$self().options.hideGui = true;
		}
	}

	@Override
	default void stopCutscene() {
		var data = vl$self().player.vl$sessionData();

		if (data.cameraOverride instanceof ClientCutscene cc) {
			cc.stopped();
		}

		data.cameraOverride = null;

		if (vl$self().screen instanceof CutsceneScreen screen) {
			vl$self().setScreen(screen.previousScreen);
		}

		vl$self().options.hideGui = false;
	}

	@Override
	default void shakeCamera(CameraShake shake) {
		if (shake.skip() || vl$self().player.isReplayCamera()) {
			return;
		}

		vl$self().player.vl$sessionData().cameraShakeInstances.add(new CameraShakeInstance(shake));

		if (shake.motionBlur()) {
			vl$self().gameRenderer.setPostEffect(CameraShake.MOTION_BLUR_EFFECT);
		}
	}

	@Override
	default void shakeCamera(CameraShake shake, Vec3 source, double maxDistance) {
		shakeCamera(shake.atDistance(vl$self().gameRenderer.getMainCamera().getPosition(), source, maxDistance));
	}

	@Override
	default void stopCameraShaking() {
		vl$self().player.vl$sessionData().cameraShakeInstances.clear();
	}

	@Override
	default void setCameraMode(int mode) {
		var player = vl$self().player;

		var session = player.vl$sessionData();

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
			vl$self().gameRenderer.clearPostEffect();
		} else {
			vl$self().gameRenderer.setPostEffect(id);
		}
	}

	@Override
	default void vl$closeScreen() {
		vl$self().popGuiLayer();
	}

	@Override
	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		vl$self().setScreen(new YesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel));
	}

	@Override
	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable) {
		vl$self().setScreen(new NumberVotingScreen(extraData, title, subtitle, max, unavailable));
	}

	@Override
	default void removeAllParticles() {
		vl$self().particleEngine.setLevel(vl$self().level);
		PhysicsParticleManager.clearAllParticles();
	}

	@Override
	default void removeZone(UUID uuid) {
		var session = vl$self().player.vl$sessionData();
		session.serverZones.remove(uuid);
		session.filteredZones.remove(uuid);
	}

	@Override
	default void redrawSections(LongList sections, boolean mainThread) {
		var levelRenderer = vl$self().levelRenderer;

		for (long section : sections) {
			int x = SectionPos.x(section);
			int y = SectionPos.y(section);
			int z = SectionPos.z(section);
			levelRenderer.setSectionDirty(x, y, z, mainThread);
		}
	}

	@Override
	default void playGlobalSound(PositionedSoundData data, WorldNumberVariables variables) {
		vl$self().getSoundManager().play(createGlobalSound(data, variables));
	}

	default SoundInstance createGlobalSound(PositionedSoundData data, WorldNumberVariables variables) {
		var mc = vl$self();

		if (data.position().isPresent()) {
			return new VidLibSoundInstance(mc.level, data, variables);
		} else {
			return SimpleSoundInstance.forUI(data.data().sound().value(), data.data().pitch(), data.data().volume());
		}
	}

	@Override
	default void physicsParticles(PhysicsParticleData data, long spawnTime, long seed, List<PositionedBlock> blocks) {
		if (blocks.isEmpty()) {
			return;
		}

		var realTime = vl$level().getGameTime();

		if (spawnTime < realTime - 60L || spawnTime > realTime + 60L + (long) data.lifespan.max()) {
			VidLib.LOGGER.info("Discarded physics particles packet @ " + realTime + " from " + spawnTime);
			return;
		}

		var particles = new PhysicsParticles(data, vl$level(), spawnTime, seed == 0L ? vl$self().level.getRandom().nextLong() : seed);

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
		var particles = vl$self().particleEngine;

		for (var entry : map.entrySet()) {
			for (var pos : entry.getValue()) {
				particles.createParticle(entry.getKey(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
			}
		}
	}

	@Override
	default void lineParticles(Map<LineParticleOptions, List<AABB>> map) {
		var particles = vl$self().particleEngine;

		for (var entry : map.entrySet()) {
			for (var box : entry.getValue()) {
				particles.createParticle(entry.getKey(), box.minX, box.minY, box.minZ, box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ);
			}
		}
	}

	@Override
	default void textParticles(TextParticleOptions options, List<Vec3> positions) {
		var particles = vl$self().particleEngine;

		for (var pos : positions) {
			particles.createParticle(options, pos.x, pos.y, pos.z, 0D, 0D, 0D);
		}
	}

	@Override
	default void itemParticles(ItemParticleOptions options, List<Pair<Vec3, Vec3>> positions) {
		var particles = vl$self().particleEngine;

		for (var pair : positions) {
			var pos = pair.getFirst();
			var vel = pair.getSecond();
			particles.createParticle(options, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
		}
	}

	@Override
	default void windParticles(RandomSource random, WindData data) {
		var particles = vl$self().particleEngine;
		var pos = data.data().position();

		for (int i = 0; i < data.data().count(); i++) {
			var x = pos.x + random.nextFloat();
			var y = pos.y + random.nextFloat() * (data.options().ground() ? 0.12D : 1D);
			var z = pos.z + random.nextFloat();
			var v = data.data().delta(random);
			particles.createParticle(data.options(), x, y, z, v.x(), v.y(), v.z());
		}
	}

	@Override
	default void fireParticles(RandomSource random, FireData data) {
		var particles = vl$self().particleEngine;
		var pos = data.data().position();
		var options = data.options().withResolvedGradient();

		for (int i = 0; i < data.data().count(); i++) {
			var x = pos.x + random.nextFloat();
			var y = pos.y + random.nextFloat();
			var z = pos.z + random.nextFloat();
			var v = data.data().delta(random);
			particles.createParticle(options, x, y, z, v.x(), v.y(), v.z());
		}
	}

	@Override
	default void setScreenFade(Fade fade) {
		if (vl$self().player.isReplayCamera()) {
			return;
		}

		vl$self().player.vl$sessionData().screenFade = new ScreenFadeInstance(fade);
	}

	@Override
	default void marker(MarkerData data) {
		// VidLib.LOGGER.info("Marker " + data.event() + "/" + data.name() + " (" + data.uuid() + ") @ ");
	}

	@Override
	default GameProfile retrieveGameProfile(UUID uuid) {
		try {
			var profile = vl$self().getMinecraftSessionService().fetchProfile(uuid, true).profile();
			return profile == null ? Empty.PROFILE : profile;
		} catch (Exception ex) {
			return Empty.PROFILE;
		}
	}

	@Override
	default GameProfile retrieveGameProfile(String name) {
		try {
			return MiscUtils.fetchProfile(name);
		} catch (Exception ex) {
			return Empty.PROFILE;
		}
	}
}
