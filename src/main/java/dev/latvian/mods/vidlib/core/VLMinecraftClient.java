package dev.latvian.mods.vidlib.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.WorldMouse;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.camera.DetachedCamera;
import dev.latvian.mods.vidlib.feature.camera.FreeCamera;
import dev.latvian.mods.vidlib.feature.camera.ScreenShake;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeInstance;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.cutscene.ClientCutscene;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneScreen;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.DataMapValue;
import dev.latvian.mods.vidlib.feature.data.UpdatePlayerDataValuePayload;
import dev.latvian.mods.vidlib.feature.data.UpdateServerDataValuePayload;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.misc.MarkerData;
import dev.latvian.mods.vidlib.feature.particle.FireData;
import dev.latvian.mods.vidlib.feature.particle.ItemParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.LineParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.ShapeParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.TextParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.WindData;
import dev.latvian.mods.vidlib.feature.particle.physics.PalettePhysicsParticlesData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesIdData;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.Fade;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.ScreenFadeInstance;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.VidLibSoundInstance;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import dev.latvian.mods.vidlib.feature.vote.NumberVotingScreen;
import dev.latvian.mods.vidlib.feature.vote.YesNoVotingScreen;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.latvian.mods.vidlib.util.PauseType;
import dev.latvian.mods.vidlib.util.ScheduledTask;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PlayerSkin;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("resource")
public interface VLMinecraftClient extends VLMinecraftEnvironment {
	Cache<String, SystemToast.SystemToastId> SYSTEM_TOAST_IDS = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

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

	@Nullable
	default WorldMouse getWorldMouse() {
		return vl$self().player.vl$sessionData().worldMouse;
	}

	@ApiStatus.Internal
	default void vl$renderSetup(FrameGraphSetupEvent event) {
		var mc = vl$self();
		var player = vl$self().player;

		if (player == null || mc.level == null) {
			return;
		}

		var session = player.vl$sessionData();
		var frameInfo = new FrameInfo(mc, session, event);
		session.worldMouse = WorldMouse.of(mc, frameInfo.camera().getPosition());
		FrameInfo.CURRENT = frameInfo;

		var rayLine = vl$self().gameRenderer.getMainCamera().ray(512D);
		var ray = new ClipContext(rayLine.start(), rayLine.end(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);

		if (vl$self().options.getCameraType() == CameraType.FIRST_PERSON && VidLibClientOptions.getShowZones()) {
			session.zoneClip = session.filteredZones.clip(ray);
		} else {
			session.zoneClip = null;
		}

		var tool = VidLibTool.of(player);

		var screenDelta = event.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		if (tool != null) {
			tool.getSecond().renderSetup(player, tool.getFirst(), mc.hitResult, screenDelta);
		}

		GhostStructure.preRender(frameInfo, mc.level.getGlobalContext());

		if (session.npcRecording != null) {
			session.npcRecording.record(System.currentTimeMillis(), screenDelta, mc.player);
		}

		CanvasImpl.createHandles(mc, event.getFrameGrapBuilder(), event.getRenderTargetDescriptor());
		// event.enableOutlineProcessing();
	}

	default Vector2dc vl$getCameraShakeOffset(float delta) {
		var player = vl$self().player;

		if (player != null) {
			var s = player.vl$sessionData();
			return s.prevCameraShake.lerp(s.cameraShake, delta, new Vector2d());
		}

		return Identity.DVEC_2;
	}

	default void vl$applyCameraShake(Camera camera, float delta) {
		var shake = vl$getCameraShakeOffset(delta);

		if (shake != Identity.DVEC_2) {
			var vec = new Vector4f((float) shake.x(), (float) shake.y(), 0F, 1F).rotate(camera.rotation());
			camera.vl$setPosition(camera.getPosition().add(vec.x(), vec.y(), vec.z()));
		}
	}

	default void updatePlayerData(List<DataMapValue> update) {
		c2s(new UpdatePlayerDataValuePayload(update));
	}

	default <T> void updatePlayerData(DataKey<T> type, T value) {
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
	default void toast(String uniqueId, long displayTime, Component title, Component description) {
		SystemToast.SystemToastId id;

		if (uniqueId.isEmpty()) {
			id = new SystemToast.SystemToastId(displayTime);
		} else {
			try {
				id = SYSTEM_TOAST_IDS.get(uniqueId, () -> new SystemToast.SystemToastId(displayTime));
			} catch (Exception e) {
				id = new SystemToast.SystemToastId(displayTime);
			}
		}

		SystemToast.addOrUpdate(vl$self().getToastManager(), id, title, description);
	}

	@Override
	default void playCutscene(Cutscene cutscene, KNumberVariables variables) {
		var player = vl$self().player;

		if (!cutscene.steps.isEmpty() && player != null) {
			var overrideCamera = !player.isReplayCamera();
			var inst = new ClientCutscene(vl$self(), overrideCamera, cutscene, variables, player::getEyePosition);
			player.vl$sessionData().currentCutscene = inst;
			player.vl$sessionData().cameraOverride = inst;

			if (overrideCamera && !cutscene.allowMovement) {
				vl$self().setScreen(new CutsceneScreen(inst, vl$self().screen));
			}
		}
	}

	@Override
	default void stopCutscene() {
		var data = vl$self().player.vl$sessionData();

		if (data.currentCutscene != null) {
			data.currentCutscene.stopped();
		}

		data.cameraOverride = null;
		data.currentCutscene = null;

		if (vl$self().screen instanceof CutsceneScreen screen) {
			vl$self().setScreen(screen.previousScreen);
		}
	}

	@Override
	default void screenShake(ScreenShake shake) {
		if (shake.skip() || vl$self().player.isReplayCamera()) {
			return;
		}

		vl$self().player.vl$sessionData().screenShakeInstances.add(new ScreenShakeInstance(shake));

		if (shake.motionBlur()) {
			vl$self().gameRenderer.setPostEffect(ScreenShake.MOTION_BLUR_EFFECT);
		}
	}

	@Override
	default void screenShake(ScreenShake shake, Vec3 source, double maxDistance) {
		screenShake(shake.atDistance(vl$self().gameRenderer.getMainCamera().getPosition(), source, maxDistance));
	}

	@Override
	default void stopScreenShake() {
		vl$self().player.vl$sessionData().screenShakeInstances.clear();
	}

	@Override
	default void setCameraMode(int mode) {
		var player = vl$self().player;

		var session = player.vl$sessionData();

		if (session.currentCutscene != null) {
			stopCutscene();
		}

		if (mode == 3) {
			int currentMode = 0;

			if (session.cameraOverride instanceof DetachedCamera) {
				currentMode = 1;
			} else if (session.cameraOverride instanceof FreeCamera) {
				currentMode = 2;
			}

			if (currentMode == 1) {
				mode = 2;
			} else if (currentMode == 2) {
				mode = 1;
			} else {
				mode = 2;
			}
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
				if (session.cameraOverride instanceof DetachedCamera(Vec3 position, Rotation rotation)) {
					session.cameraOverride = new FreeCamera(position, rotation);
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
	default void removeZone(ResourceLocation zone, int index) {
		var session = vl$self().player.vl$sessionData();
		session.serverZones.remove(zone, index);
		session.filteredZones.remove(zone, index);
	}

	@Override
	default void updateZone(ResourceLocation zone, int index, Zone zoneData) {
		var session = vl$self().player.vl$sessionData();
		session.serverZones.update(zone, index, zoneData);
		session.filteredZones.update(zone, index, zoneData);
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
	default void playGlobalSound(PositionedSoundData data, KNumberVariables variables) {
		vl$self().getSoundManager().play(createGlobalSound(data, variables));
	}

	default SoundInstance createGlobalSound(PositionedSoundData data, KNumberVariables variables) {
		var mc = vl$self();

		if (mc.level != null && data.position().isPresent()) {
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
	default void physicsParticles(PalettePhysicsParticlesData data, long spawnTime) {
		if (!data.positions().isEmpty()) {
			// FIXME
		}
	}

	@Override
	default void cubeParticles(Map<ShapeParticleOptions, List<BlockPos>> map) {
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
	default void setInfoBarText(int bar, Component text) {
		var session = vl$self().player.vl$sessionData();

		switch (bar) {
			case 0 -> session.topInfoBarOverride = text;
			case 1 -> session.bottomInfoBarOverride = text;
		}
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

	@Override
	default KNumberVariables globalVariables() {
		return vl$self().player.vl$sessionData().globalVariables;
	}

	default float linearizeDepth(float depth) {
		float near = 0.05F;
		float far = vl$self().gameRenderer.getDepthFar();
		return near * far / (far + depth * (near - far));
	}

	default boolean vl$hideGui() {
		var mc = vl$self();

		if (mc.options.hideGui) {
			return true;
		} else if (mc.screen != null && mc.screen.hideGui()) {
			return true;
		} else if (mc.player == null) {
			return false;
		}

		var session = mc.player.vl$sessionData();
		return session.cameraOverride != null && session.cameraOverride.hideGui();
	}

	default TextureAtlas getBlockAtlas() {
		return vl$self().getModelManager().getAtlas(SpriteKey.BLOCKS);
	}

	default TextureAtlas getParticleAtlas() {
		return vl$self().particleEngine.getTextureAtlas();
	}

	default TextureAtlas getGuiAtlas() {
		return vl$self().getGuiSprites().textureAtlas;
	}

	default TextureAtlas getTextureAtlas(SpriteKey sprite) {
		if (sprite.atlas() == SpriteKey.BLOCKS) {
			return getBlockAtlas();
		} else if (sprite.atlas() == SpriteKey.PARTICLES) {
			return getParticleAtlas();
		} else if (sprite.atlas() == SpriteKey.GUI) {
			return getGuiAtlas();
		} else {
			return vl$self().getModelManager().getAtlas(sprite.atlas());
		}
	}

	default TextureAtlasSprite getSprite(SpriteKey sprite) {
		return getTextureAtlas(sprite).getSprite(sprite.sprite());
	}

	default <T> void updateServerDataValue(DataKey<T> key, T value) {
		c2s(new UpdateServerDataValuePayload(List.of(new DataMapValue(key, value))));
	}

	default void runClientCommand(String command) {
		vl$self().player.connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
	}

	@Override
	default List<ClientSessionData> vl$getAllSessionData() {
		return vl$self().player.vl$sessionData().getAllClientSessionData();
	}

	@Override
	default Map<ResourceLocation, ClockValue> vl$getClocks() {
		return vl$self().player.vl$sessionData().clocks;
	}

	@Override
	@Nullable
	default String getServerBrand() {
		var p = vl$self().player;
		return p == null ? null : p.connection.serverBrand();
	}

	@Override
	default boolean isServerNeoForge() {
		var p = vl$self().player;
		return p != null && p.vl$sessionData().isServerNeoForge();
	}

	default PlayerSkin.Model getModelType(@Nullable GameProfile profile) {
		if (profile == null || profile == Empty.PROFILE) {
			return PlayerSkin.Model.WIDE;
		}

		try {
			var sessionService = vl$self().getMinecraftSessionService();
			var packedTextures = sessionService.getPackedTextures(profile);

			if (packedTextures != null) {
				var unpackedTextures = sessionService.unpackTextures(packedTextures);

				if (unpackedTextures.skin() != null) {
					return PlayerSkin.Model.byName(unpackedTextures.skin().getMetadata("model"));
				}
			}

		} catch (Exception ignored) {
		}

		return PlayerSkin.Model.WIDE;
	}

	default float getEffectScale() {
		return vl$self().getWindow().getGuiScaledHeight() / 1080F;
	}
}
