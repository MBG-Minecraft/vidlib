package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.core.api.client.KLibLocalPlayer;
import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.klib.math.ProjectedCoordinates;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.particle.ShapeParticleOptions;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.core.api.client.VLMinecraftClient;
import dev.latvian.mods.vidlib.feature.VLFeatures;
import dev.latvian.mods.vidlib.feature.actor.ActorParticleOptions;
import dev.latvian.mods.vidlib.feature.actor.ActorRecording;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.atmosphere.ClientAtmosphere;
import dev.latvian.mods.vidlib.feature.camera.ControlledCameraOverride;
import dev.latvian.mods.vidlib.feature.camera.DetachedCamera;
import dev.latvian.mods.vidlib.feature.camera.FreeCamera;
import dev.latvian.mods.vidlib.feature.camera.ScreenShake;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeInstance;
import dev.latvian.mods.vidlib.feature.cutscene.ClientCutscene;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneScreen;
import dev.latvian.mods.vidlib.feature.data.DataMapOverrides;
import dev.latvian.mods.vidlib.feature.data.InternalGlobalData;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.environment.WorldBorderOverride;
import dev.latvian.mods.vidlib.feature.imgui.BuiltInImGui;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChangedEvent;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToServer;
import dev.latvian.mods.vidlib.feature.maptextureoverride.MapTextureOverridesReplaySessionData;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.misc.DebugTextEvent;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import dev.latvian.mods.vidlib.feature.note.Note;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesDisplayData;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectInstance;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.Fade;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.ScreenFadeInstance;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.VidLibSoundInstance;
import dev.latvian.mods.vidlib.feature.vote.NumberVotingScreen;
import dev.latvian.mods.vidlib.feature.vote.YesNoVotingScreen;
import dev.latvian.mods.vidlib.feature.zone.ResolvedZone;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.feature.zone.ZoneHit;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4f;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClientSessionData extends GlobalSessionData implements VLFeatures {
	public static ClientSessionData of(Minecraft mc) {
		return mc.vl$getGlobalData();
	}

	public final Minecraft mc;
	public final LocalPlayerSessionData localPlayer;
	public ClientLevelSessionData currentLevel;

	public ZoneHit zoneHit;
	public final List<ScreenShakeInstance> screenShakeInstances;
	public Vector2dc prevCameraShake;
	public Vector2dc cameraShake;
	public final Map<String, ClientAtmosphere> clientAtmospheres;
	public final KNumberVariables globalVariables;
	public CameraOverride cameraOverride;
	public ClientCutscene currentCutscene;
	public ScreenFadeInstance screenFade;
	public ProjectedCoordinates projectedCoordinates;
	public ActorRecording actorRecording;
	public final List<Decal> debugDecals;
	public final List<ScreenEffectInstance> screenEffects;
	public Component topInfoBarOverride;
	public Component bottomInfoBarOverride;

	public Map<UUID, Color> glowColors;
	public WorldBorderOverride worldBorderOverrideStart;
	public WorldBorderOverride worldBorderOverrideEnd;
	public boolean clientModListSentDuringConfig;
	public List<ReplayMarkerData> markers;
	public Map<UUID, Note> notes;
	public final Int2IntMap entityBrightnessOverrides;

	public ClientSessionData(Minecraft mc, RegistryAccess registryAccess, UUID localPlayerUUID) {
		super(registryAccess);
		this.mc = mc;
		this.localPlayer = new LocalPlayerSessionData(this, localPlayerUUID);

		this.zoneHit = null;
		this.screenShakeInstances = new ArrayList<>();
		this.prevCameraShake = this.cameraShake = Identity.DVEC_2;
		this.clientAtmospheres = new Reference2ObjectOpenHashMap<>();
		this.globalVariables = new KNumberVariables();
		this.debugDecals = new ArrayList<>();
		this.screenEffects = new ArrayList<>();
		this.clientModListSentDuringConfig = false;
		this.markers = new ArrayList<>();
		this.notes = new Object2ObjectLinkedOpenHashMap<>();
		this.entityBrightnessOverrides = new Int2IntOpenHashMap();
		this.entityBrightnessOverrides.defaultReturnValue(-1);

		VidLib.LOGGER.info("Client Session Data Initialized");

		if (mc.player != null) {
			isReplay = mc.player.getClass() != LocalPlayer.class || mc.player.getScoreboardName().equals("Replay_Viewer") || mc.player.getGameProfile().properties().containsKey("IsReplayViewer");
		}
	}

	@Override
	public long getLevelGameTime() {
		return mc.level == null ? 0L : mc.level.getGameTime();
	}

	public void levelChanged(@Nullable ClientLevel level) {
		if (currentLevel != null) {
			currentLevel.props = null;
			currentLevel = null;
		}

		updateOps(level == null ? null : level.registryAccess());

		if (level != null) {
			currentLevel = getLevel(level.dimension());
			currentLevel.props = new ClientProps(currentLevel, level);
		}
	}

	@Override
	public ClientLevelSessionData getLevel(ResourceKey<Level> dimension) {
		return (ClientLevelSessionData) super.getLevel(dimension);
	}

	@Override
	protected ClientLevelSessionData createLevelData(ResourceKey<Level> dimension) {
		return new ClientLevelSessionData(this, dimension);
	}

	@Override
	public ClientPlayerSessionData getPlayer(UUID uuid) {
		return (ClientPlayerSessionData) super.getPlayer(uuid);
	}

	@Override
	protected ClientPlayerSessionData createPlayerData(UUID uuid) {
		return uuid.equals(localPlayer.uuid) ? localPlayer : new RemotePlayerSessionData(this, uuid);
	}

	@Override
	public PauseType getPauseType() {
		return mc.isPaused() ? PauseType.GAME : mc.level != null && mc.level.tickRateManager().runsNormally() ? PauseType.NONE : PauseType.TICK;
	}

	@Override
	public void preTick(PauseType pauseType) {
		super.preTick(pauseType);

		if (DataMapOverrides.INSTANCE != null) {
			dataMap.overrides = DataMapOverrides.INSTANCE.serverData;

			for (var session : levelMap.values()) {
				session.dataMap.overrides = DataMapOverrides.INSTANCE.getLevelMap(session.dimension);
			}

			for (var session : playerMap.values()) {
				session.dataMap.overrides = DataMapOverrides.INSTANCE.getPlayerMap(session.uuid);
			}
		}

		if (mc.level != null) {
			VLPlatformHelper.INSTANCE.getLevelData(mc.level).tick(mc.level, pauseType);
		}

		if (mc.player != null) {
			localPlayer.input = KLibLocalPlayer.fromInput(mc, mc.player/*, mc.gui.screen() == null && mc.isWindowActive()*/);

			if (!localPlayer.prevInput.equals(localPlayer.input)) {
				NeoForge.EVENT_BUS.post(new PlayerInputChangedEvent(mc.player, localPlayer.prevInput, localPlayer.input));
				localPlayer.prevInput = localPlayer.input;
				mc.player.c2s(new SyncPlayerInputToServer(localPlayer.input));
			}

			if (mc.options.keyJump.isDown() && PlayerActionHandler.handle(mc.player, PlayerActionType.JUMP, true)) {
				KeyMapping.set(mc.options.keyJump.getKey(), false);
				mc.options.keyJump.setDown(false);
			}

			if (mc.options.keyShift.isDown() && !mc.hasAltDown() && PlayerActionHandler.handle(mc.player, PlayerActionType.SNEAK, true)) {
				KeyMapping.set(mc.options.keyShift.getKey(), false);
				mc.options.keyShift.setDown(false);
			}

			if (mc.options.keySprint.isDown() && PlayerActionHandler.handle(mc.player, PlayerActionType.SPRINT, true)) {
				KeyMapping.set(mc.options.keySprint.getKey(), false);
				mc.options.keySprint.setDown(false);
			}
		}
	}

	@Override
	public void postTick(PauseType pauseType) {
		super.postTick(pauseType);

		if (currentCutscene != null && currentCutscene.tick()) {
			stopCutscene();
		}

		if (cameraOverride instanceof ControlledCameraOverride c && c.tick()) {
			cameraOverride = null;
		}

		if (screenFade != null && screenFade.tick()) {
			screenFade = null;
		}

		prevCameraShake = cameraShake;
		double shakeX = 0D;
		double shakeY = 0D;

		if (!screenShakeInstances.isEmpty()) {
			var shakeIt = screenShakeInstances.iterator();

			while (shakeIt.hasNext()) {
				var instance = shakeIt.next();
				var vec = instance.shake.type().value().get(instance.progress);
				var intensity = instance.shake.intensity();
				var intensityScale = instance.shake.ease().apply(instance.ticks / (float) instance.shake.duration());
				shakeX += vec.x() * intensity * intensityScale;
				shakeY += vec.y() * intensity * intensityScale;

				instance.progress += instance.shake.speed();

				if (++instance.ticks >= instance.shake.duration()) {
					shakeIt.remove();
				}
			}
		}

		cameraShake = Math.abs(shakeX) <= 0.0001D && Math.abs(shakeY) <= 0.0001D ? Identity.DVEC_2 : new Vector2d(shakeX, shakeY);

		int undo = ClientLevelSessionData.of(mc.level).undoAllFutureModifications(mc.level, false);

		if (undo > 0) {
			VidLib.LOGGER.info("Undone " + undo + " future modifications");
		}

		BuiltInImGui.OPEN_PANELS.values().forEach(Panel::tick);

		if (mc.level != null && pauseType.tick()) {
			PhysicsParticleManager.tickAll(mc.level, mc.level.getGameTime());
		}

		NeoForge.EVENT_BUS.post(new DebugTextEvent.ClientTick(ScreenText.CLIENT_TICK));

		/*
		int b = StructureCapture.CURRENT.getValue().blocks.size();

		if (b > 0) {
			ScreenText.CLIENT_TICK.topLeft.add("Ghost Structure Capture: %,d blocks".formatted(b));
		}
		 */

		if (!MiscClientUtils.CLIENT_CLOSEABLE.isEmpty()) {
			for (var c : MiscClientUtils.CLIENT_CLOSEABLE) {
				try {
					c.close();
				} catch (Exception ignored) {
				}
			}

			MiscClientUtils.CLIENT_CLOSEABLE.clear();
		}

		HubClientGateway.tickGateway();
	}

	@Override
	public void removePlayer(UUID uuid) {
		super.removePlayer(uuid);

		if (uuid.equals(localPlayer.uuid)) {
			mc.getConnection().disconnect(Component.literal("Your player data was reset"));
		}
	}

	@Override
	public void setClientModListSentDuringConfig() {
		clientModListSentDuringConfig = true;
	}

	@Nullable
	public SpriteKey getMapTextureOverride(int id) {
		var data = ReplayAPI.getActiveSessionData(MapTextureOverridesReplaySessionData.TYPE);

		if (data != null) {
			var override = data.overrides.get(id);

			if (override != null) {
				return override;
			}
		}

		var overrides = dataMap.getOptional(InternalGlobalData.MAP_TEXTURE_OVERRIDES);
		return overrides == null ? null : overrides.get(id);
	}

	public ClientAtmosphere getAtmosphere(Ref<Atmosphere> ref) {
		var atmosphere = clientAtmospheres.get(ref.key());

		if (atmosphere == null) {
			atmosphere = new ClientAtmosphere(ref);
			clientAtmospheres.put(ref.key(), atmosphere);
		}

		return atmosphere;
	}

	public void startActorRecording(NameAndId profile) {
		if (actorRecording == null) {
			actorRecording = new ActorRecording(profile);
			actorRecording.record(actorRecording.start, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true), mc.player);
		} else {
			mc.player.sendSystemMessage(Component.literal("Already recording Actor '" + profile.name() + "'!"));
		}
	}

	public void stopActorRecording() {
		if (actorRecording != null) {
			actorRecording.length = System.currentTimeMillis() - actorRecording.start;

			var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), mc.level.registryAccess());
			var path = VidLibPaths.ACTOR_RECORDINGS.get().resolve(actorRecording.start + "_" + actorRecording.profile.name().toLowerCase(Locale.ROOT) + ".arec");

			if (Files.notExists(path.getParent())) {
				try {
					Files.createDirectories(path.getParent());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			try (var out = new BufferedOutputStream(Files.newOutputStream(path))) {
				actorRecording.write(buf);
				buf.readBytes(out, buf.readableBytes());
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			actorRecording = null;
			ActorRecording.REPLAY = null;
			mc.player.sendSystemMessage(Component.literal("Actor recording '" + path.getFileName() + "' saved!"));
		}
	}

	public void replayActorRecording() {
		replayActorRecording("latest");
	}

	public void replayActorRecording(String name) {
		var map = ActorRecording.getReplay(mc.level.registryAccess());

		if (!map.containsKey(name)) {
			mc.player.sendSystemMessage(Component.literal("Actor recording '" + name + "' not found!"));
			return;
		}

		mc.level.addParticle(new ActorParticleOptions(name, false, 0, Optional.empty()), true, true, mc.player.getX(), mc.player.getY(), mc.player.getZ(), 0D, 0D, 0D);
	}

	public Vector2dc getCameraShakeOffset(float delta) {
		if (prevCameraShake == Identity.DVEC_2 && cameraShake == Identity.DVEC_2) {
			return Identity.DVEC_2;
		}

		return prevCameraShake.lerp(cameraShake, delta, new Vector2d());
	}

	public void applyCameraShake(Camera camera, float delta) {
		var shake = getCameraShakeOffset(delta);

		if (shake != Identity.DVEC_2) {
			var vec = new Vector4f((float) shake.x(), (float) shake.y(), 0F, 1F).rotate(camera.rotation());
			camera.klib$setPosition(camera.position().add(vec.x(), vec.y(), vec.z()));
		}
	}

	public void updateZones(Collection<Ref<Zone>> values) {
		for (var level : levelMap.values()) {
			var list = new ArrayList<ResolvedZone>();

			for (var ref : values) {
				if (ref.value().dimension() == level.dimension) {
					list.add(new ResolvedZone(level.zones, ref));
				}
			}

			level.zones.update(list);
		}
	}

	public void updateAtmospheres(Collection<Ref<Atmosphere>> values) {
		clientAtmospheres.clear();

		for (var ref : values) {
			clientAtmospheres.put(ref.key(), new ClientAtmosphere(ref));
		}
	}

	// Packet Handlers

	@Override
	public void updateInput(Level level, UUID player, PlayerInput input) {
		var data = getPlayer(player);
		data.prevInput = data.input = input;

		if (level.getEntity(player) instanceof Player entity) {
			var vehicle = entity.getVehicle();

			if (vehicle != null) {
				vehicle.setPilotInput(entity, input);
			}
		}
	}

	@Override
	public void createNote(Note note) {
		notes.put(note.id(), note);
	}

	@Override
	public void deleteNote(UUID id) {
		notes.remove(id);
	}

	@Override
	public void setEntityBrightnessOverride(int entityId, int override) {
		if (override == -1) {
			entityBrightnessOverrides.remove(entityId);
		} else {
			entityBrightnessOverrides.put(entityId, override);
		}
	}

	@Override
	public void toast(String uniqueId, long displayTime, Component title, Component description) {
		SystemToast.SystemToastId id;

		if (uniqueId.isEmpty()) {
			id = new SystemToast.SystemToastId(displayTime);
		} else {
			try {
				id = VLMinecraftClient.SYSTEM_TOAST_IDS.get(uniqueId, () -> new SystemToast.SystemToastId(displayTime));
			} catch (Exception e) {
				id = new SystemToast.SystemToastId(displayTime);
			}
		}

		SystemToast.addOrUpdate(mc.gui.toastManager(), id, title, description);
	}

	@Override
	public void playCutscene(Ref<Cutscene> cutscene, KNumberVariables variables) {
		var player = mc.player;
		var c = cutscene.value();

		if (!c.steps().isEmpty() && player != null) {
			var overrideCamera = !isReplay;
			var inst = new ClientCutscene(mc, overrideCamera, c, variables, mc.player::getEyePosition);
			currentCutscene = inst;
			cameraOverride = inst;

			if (overrideCamera && !c.allowMovement()) {
				mc.gui.setScreen(new CutsceneScreen(inst, mc.gui.screen()));
			}
		}
	}

	@Override
	public void stopCutscene() {
		if (currentCutscene != null) {
			currentCutscene.stopped();
		}

		cameraOverride = null;
		currentCutscene = null;

		if (mc.gui.screen() instanceof CutsceneScreen screen) {
			mc.gui.setScreen(screen.previousScreen);
		}
	}

	@Override
	public void screenShake(ScreenShake shake) {
		if (shake.skip() || isReplay) {
			return;
		}

		screenShakeInstances.add(new ScreenShakeInstance(shake));

		if (shake.motionBlur()) {
			mc.player.setActivePostEffects(List.of(ScreenShake.MOTION_BLUR_EFFECT));
		}
	}

	@Override
	public void screenShake(ScreenShake shake, Vec3 source, double maxDistance) {
		screenShake(shake.atDistance(mc.gameRenderer.mainCamera().position(), source, maxDistance));
	}

	@Override
	public void stopScreenShake() {
		screenShakeInstances.clear();
	}

	@Override
	public void setCameraMode(int mode) {
		var session = ClientSessionData.of(mc);

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
					session.cameraOverride = new DetachedCamera(mc.player.getEyePosition(), Rotation.of(mc.player, 1F));
				}
			}
			case 2 -> {
				if (session.cameraOverride instanceof DetachedCamera(Vec3 position, Rotation rotation)) {
					session.cameraOverride = new FreeCamera(position, rotation);
				} else {
					session.cameraOverride = new FreeCamera(mc.player.getEyePosition(), Rotation.of(mc.player, 1F));
				}
			}
			default -> stopCutscene();
		}
	}

	@Override
	public void closeScreen() {
		mc.gui.setScreen(null);
	}

	@Override
	public void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel, boolean closeOnVote) {
		mc.gui.setScreen(new YesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel, closeOnVote));
	}

	@Override
	public void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable, boolean closeOnVote) {
		mc.gui.setScreen(new NumberVotingScreen(extraData, title, subtitle, max, unavailable, closeOnVote));
	}

	@Override
	public void removeAllParticles() {
		mc.particleEngine.setLevel(mc.level);
		PhysicsParticleManager.clearAllParticles();
	}

	@Override
	public void redrawSections(LongList sections, boolean mainThread) {
		var levelExtractor = mc.levelExtractor;

		for (long section : sections) {
			levelExtractor.setSectionDirty(SectionPos.x(section), SectionPos.y(section), SectionPos.z(section));
		}
	}

	public SoundInstance createSoundInstance(PositionedSoundData data, KNumberContext ctx) {
		if (mc.level != null && data.position().isPresent()) {
			return new VidLibSoundInstance(mc.level, data, ctx);
		} else {
			return SimpleSoundInstance.forUI(data.data().sound().value(), data.data().pitch(), data.data().volume());
		}
	}

	@Override
	public void playGlobalSound(PositionedSoundData data, KNumberVariables variables) {
		mc.getSoundManager().play(createSoundInstance(data, ClientLevelSessionData.of(mc.level).createGlobalContext().fork(variables)));
	}

	@Override
	public void physicsParticles(PhysicsParticlesDisplayData displayData, long spawnTime) {
		var level = mc.level;
		var data = displayData.data().value();
		var realTime = level.getGameTime();

		if (spawnTime < realTime - 60L || spawnTime > realTime + 60L + (long) data.lifespan().max()) {
			VidLib.LOGGER.info("Discarded physics particles packet @ " + realTime + " from " + spawnTime);
			return;
		}

		var particles = new PhysicsParticles(data, level, spawnTime, displayData.seed() == 0L ? level.getRandom().nextLong() : displayData.seed());

		displayData.blocks().value().forEach((pos, state) -> {
			particles.at = pos.immutable();
			particles.state = state;
			particles.spawn();
		}, level.getRandom());
	}

	@Override
	public void shapeParticles(Map<ShapeParticleOptions, List<BlockPos>> map) {
		var particles = mc.particleEngine;

		for (var entry : map.entrySet()) {
			for (var pos : entry.getValue()) {
				particles.createParticle(entry.getKey(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
			}
		}
	}

	@Override
	public void setScreenFade(Fade fade) {
		if (!isReplay) {
			screenFade = new ScreenFadeInstance(fade);
		}
	}

	@Override
	public void marker(ReplayMarkerData data) {
		data = ClientGameEngine.INSTANCE.handleRuntimeMarker(data);

		if (data != null) {
			markers.add(data);
		}
	}

	@Override
	public void setInfoBarText(int bar, Component text) {
		switch (bar) {
			case 0 -> topInfoBarOverride = text;
			case 1 -> bottomInfoBarOverride = text;
		}
	}
}
