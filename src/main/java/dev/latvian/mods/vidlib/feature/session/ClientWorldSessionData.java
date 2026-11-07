package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.klib.math.ProjectedCoordinates;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.util.MiscUtils;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.core.VLClientCommonPacketListener;
import dev.latvian.mods.vidlib.feature.actor.ActorParticleOptions;
import dev.latvian.mods.vidlib.feature.actor.ActorRecording;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.atmosphere.ClientAtmosphere;
import dev.latvian.mods.vidlib.feature.camera.ControlledCameraOverride;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeInstance;
import dev.latvian.mods.vidlib.feature.cutscene.ClientCutscene;
import dev.latvian.mods.vidlib.feature.data.DataMapOverrides;
import dev.latvian.mods.vidlib.feature.data.DataMapValue;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.environment.WorldBorderOverride;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.imgui.BuiltInImGui;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.maptextureoverride.MapTextureOverridesReplaySessionData;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.misc.DebugTextEvent;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import dev.latvian.mods.vidlib.feature.note.Note;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectInstance;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.ScreenFadeInstance;
import dev.latvian.mods.vidlib.feature.zone.ZoneHit;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClientWorldSessionData extends WorldSessionData {
	public static ClientWorldSessionData of(Minecraft mc) {
		return of(mc.player);
	}

	public static ClientWorldSessionData of(LocalPlayer player) {
		return of(player.connection);
	}

	public static ClientWorldSessionData of(ClientCommonPacketListener listener) {
		return ((VLClientCommonPacketListener) listener).vl$getWorldData();
	}

	public final Minecraft mc;
	public final LocalClientPlayerSessionData localPlayer;
	public ClientLevelSessionData level;

	public ZoneHit zoneHit;
	public final List<ScreenShakeInstance> screenShakeInstances;
	public Vector2dc prevCameraShake;
	public Vector2dc cameraShake;
	public Map<String, ClientAtmosphere> atmosphereCache;
	public final KNumberVariables globalVariables;
	public ClientAtmosphere atmosphere;
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

	public ClientWorldSessionData(Minecraft mc, UUID localPlayerUUID) {
		super(MiscUtils.STATIC_REGISTRY_ACCESS);
		this.mc = mc;
		this.localPlayer = new LocalClientPlayerSessionData(this, mc, localPlayerUUID);

		this.zoneHit = null;
		this.screenShakeInstances = new ArrayList<>();
		this.prevCameraShake = this.cameraShake = Identity.DVEC_2;
		this.atmosphereCache = new Reference2ObjectOpenHashMap<>();
		this.globalVariables = new KNumberVariables();
		this.debugDecals = new ArrayList<>();
		this.screenEffects = new ArrayList<>();
		this.glowColors = new Object2ObjectOpenHashMap<>();
		this.clientModListSentDuringConfig = false;
		this.markers = new ArrayList<>();
		this.notes = new Object2ObjectLinkedOpenHashMap<>();
		this.entityBrightnessOverrides = new Int2IntOpenHashMap();
		this.entityBrightnessOverrides.defaultReturnValue(-1);

		VidLib.LOGGER.info("Client Session Data Initialized");
	}

	@Override
	public long getLevelGameTime() {
		return mc.level == null ? 0L : mc.level.getGameTime();
	}

	@Override
	protected ClientLevelSessionData createLevelData(Level level) {
		return new ClientLevelSessionData(this, level.dimension());
	}

	@Override
	protected PlayerSessionData createPlayerData(UUID uuid) {
		return uuid.equals(localPlayer.uuid) ? localPlayer : new RemoteClientPlayerSessionData(this, uuid);
	}

	@Override
	public PauseType getPauseType() {
		return mc.isPaused() ? PauseType.GAME : mc.level != null && mc.level.tickRateManager().runsNormally() ? PauseType.NONE : PauseType.TICK;
	}

	@Override
	public FeatureSet getServerFeatures() {
		return FeatureSet.REMOTE_SERVER_FEATURES;
	}

	@Override
	public void preTick(PauseType pauseType) {
		super.preTick(pauseType);

		if (DataMapOverrides.INSTANCE != null) {
			dataMap.overrides = DataMapOverrides.INSTANCE.serverData;

			for (var session : playerMap.values()) {
				session.dataMap.overrides = DataMapOverrides.INSTANCE.getPlayerMap(session.uuid);
			}
		}

		if (mc.level != null) {
			getLevel(mc.level.dimension()).tick(mc.level, pauseType);
		}

		localPlayer.preTick(mc.level, mc.player);
	}

	@Override
	public void postTick(PauseType pauseType) {
		super.postTick(pauseType);

		if (currentCutscene != null && currentCutscene.tick()) {
			mc.stopCutscene();
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

		int undo = level.undoAllFutureModifications(mc.level, false);

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
	public void updateOverrides() {
		super.updateOverrides();

		var atmosphereId = ClientGameEngine.INSTANCE.getAtmosphere(mc);

		if (atmosphereId == null) {
			atmosphere = null;
		} else {
			atmosphere = getAtmosphere(atmosphereId);
		}
	}

	@Override
	public void updateServerData(Player self, List<DataMapValue> update) {
		dataMap.update(null, update);
	}

	@Override
	public void updatePlayerData(Player self, UUID player, List<DataMapValue> update) {
		if (self.getUUID().equals(player)) {
			localPlayer.dataMap.update(self, update);
		} else {
			getPlayer(player).dataMap.update(self.level().getPlayerByUUID(player), update);
		}
	}

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
	public void updateAtmospheres() {
		atmosphereCache.clear();
	}

	@Override
	public void setGlowColor(@Nullable UUID uuid, @Nullable Color color) {
		if (uuid == null) {
			glowColors.clear();
		} else if (color == null) {
			glowColors.remove(uuid);
		} else {
			glowColors.put(uuid, color);
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

		var overrides = dataMap.getOptional(InternalServerData.MAP_TEXTURE_OVERRIDES);
		return overrides == null ? null : overrides.get(id);
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

	public ClientAtmosphere getAtmosphere(Ref<Atmosphere> ref) {
		var atmosphere = atmosphereCache.get(ref.key());

		if (atmosphere == null) {
			atmosphere = new ClientAtmosphere(ref);
			atmosphereCache.put(ref.key(), atmosphere);
		}

		return atmosphere;
	}

	public List<PlayerInfo> getListedPlayers() {
		if (mc.player == null) {
			return List.of();
		}

		var stream = mc.player.connection.getListedOnlinePlayers().stream();

		if (!ClientGameEngine.INSTANCE.canSeeAllPlayersInList(mc.player)) {
			stream = stream.filter(info -> ClientGameEngine.INSTANCE.canSeePlayerInList(mc.player, info));
		}

		return stream.sorted(PlayerTabOverlay.PLAYER_COMPARATOR).limit(80L).toList();
	}

	public void startActorRecording(Minecraft mc, NameAndId profile) {
		if (actorRecording == null) {
			actorRecording = new ActorRecording(profile);
			actorRecording.record(actorRecording.start, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true), mc.player);
		} else {
			mc.tell(Component.literal("Already recording Actor '" + profile.name() + "'!"));
		}
	}

	public void stopActorRecording(Minecraft mc) {
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
			mc.tell(Component.literal("Actor recording '" + path.getFileName() + "' saved!"));
		}
	}

	public void replayActorRecording(Minecraft mc) {
		replayActorRecording(mc, "latest");
	}

	public void replayActorRecording(Minecraft mc, String name) {
		var map = ActorRecording.getReplay(mc.level.registryAccess());

		if (!map.containsKey(name)) {
			mc.tell(Component.literal("Actor recording '" + name + "' not found!"));
			return;
		}

		mc.level.addParticle(new ActorParticleOptions(name, false, 0, Optional.empty()), true, true, mc.player.getX(), mc.player.getY(), mc.player.getZ(), 0D, 0D, 0D);
	}

	public void respawned(ClientLevel level, boolean login) {
	}
}
