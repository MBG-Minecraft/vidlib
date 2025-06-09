package dev.latvian.mods.vidlib.feature.session;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Window;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.klib.math.VoxelShapeBox;
import dev.latvian.mods.klib.math.WorldMouse;
import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.GameEventHandler;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.core.VLLocalPlayer;
import dev.latvian.mods.vidlib.feature.camera.ControlledCameraOverride;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeInstance;
import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.cutscene.ClientCutscene;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.DataMapValue;
import dev.latvian.mods.vidlib.feature.data.DataRecorder;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.fade.ScreenFadeInstance;
import dev.latvian.mods.vidlib.feature.highlight.TerrainHighlightInstance;
import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChanged;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToServer;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.npc.NPCParticleOptions;
import dev.latvian.mods.vidlib.feature.npc.NPCRecording;
import dev.latvian.mods.vidlib.feature.registry.SyncedRegistry;
import dev.latvian.mods.vidlib.feature.skybox.ClientFogOverride;
import dev.latvian.mods.vidlib.feature.skybox.Skybox;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.skybox.Skyboxes;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneEvent;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;
import dev.latvian.mods.vidlib.util.PauseType;
import dev.latvian.mods.vidlib.util.ScheduledTask;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LocalClientSessionData extends ClientSessionData {
	public final Minecraft mc;
	public final ClientPacketListener connection;
	private final Map<UUID, RemoteClientSessionData> remoteSessionData;
	private ScheduledTask.Handler scheduledTaskHandler;
	public final ActiveZones serverZones;
	public final ActiveZones filteredZones;
	public ZoneClipResult zoneClip;
	public final List<ScreenShakeInstance> screenShakeInstances;
	public Vector2dc prevCameraShake;
	public Vector2dc cameraShake;
	public Map<ResourceLocation, ClockValue> clocks;
	public Map<ResourceLocation, Skybox> skyboxes;
	public final DataMap serverDataMap;
	public final WorldNumberVariables globalVariables;
	public Skybox skybox;
	public Map<ZoneShape, VoxelShapeBox> cachedZoneShapes;
	public List<PlayerInfo> originalListedPlayers;
	public CameraOverride cameraOverride;
	public ScreenFadeInstance screenFade;
	public WorldMouse worldMouse;
	public DataRecorder dataRecorder;
	public NPCRecording npcRecording;
	public final List<TerrainHighlightInstance> terrainHighlights;

	public LocalClientSessionData(Minecraft mc, UUID uuid, ClientPacketListener connection) {
		super(uuid);
		this.mc = mc;
		this.connection = connection;
		this.remoteSessionData = new HashMap<>();

		this.serverZones = new ActiveZones();
		this.filteredZones = new ActiveZones();
		this.zoneClip = null;
		this.screenShakeInstances = new ArrayList<>();
		this.prevCameraShake = this.cameraShake = Identity.DVEC_2;
		this.clocks = new HashMap<>();
		this.skyboxes = new HashMap<>();
		this.serverDataMap = new DataMap(uuid, DataKey.SERVER);
		this.globalVariables = new WorldNumberVariables();
		this.terrainHighlights = new ArrayList<>();
		VidLib.LOGGER.info("Client Session Data Initialized");
	}

	public RemoteClientSessionData getRemoteSessionData(UUID id) {
		var data = remoteSessionData.get(id);

		if (data == null) {
			data = new RemoteClientSessionData(id);
			remoteSessionData.put(id, data);
		}

		return data;
	}

	public ClientSessionData getClientSessionData(UUID id) {
		return id.equals(uuid) ? this : getRemoteSessionData(id);
	}

	public ScheduledTask.Handler getScheduledTaskHandler() {
		if (scheduledTaskHandler == null) {
			scheduledTaskHandler = new ScheduledTask.Handler(mc, () -> mc.level);
		}

		return scheduledTaskHandler;
	}

	@Override
	public void updateOverrides(Player player) {
		super.updateOverrides(player);
		var skyboxId = EntityOverride.SKYBOX.get(player);

		if (skyboxId == null) {
			skyboxId = player.level().getSkybox();
		}

		if (skyboxId == null || skyboxId.equals(Skyboxes.VANILLA)) {
			skybox = null;
		} else {
			skybox = skyboxes.get(skyboxId);

			if (skybox == null) {
				var skyboxData = SkyboxData.REGISTRY.get(skyboxId);

				if (skyboxData == null) {
					skyboxData = new SkyboxData(skyboxId, Optional.empty(), 0F, 0F, Color.WHITE, false, true, true, Optional.empty(), Optional.empty());
				}

				skybox = new Skybox(skyboxData);
				skyboxes.put(skyboxId, skybox);
			}
		}

		GameEventHandler.ambientLight = EntityOverride.AMBIENT_LIGHT.get(player, Range.FULL);

		var f = EntityOverride.FOG.get(player);
		ClientFogOverride.override = f == null ? FogParameters.NO_FOG : ClientFogOverride.convert(f);
	}

	@Override
	public void respawned(Level level, boolean loggedIn) {
		refreshZones(level.dimension());
	}

	@ApiStatus.Internal
	public void preTick(ClientLevel level, LocalPlayer player, Window window, PauseType paused) {
		if (dataRecorder == null && !VidLibConfig.debugS2CPackets && player.isReplayCamera()) {
			dataRecorder = initDataRecorder(player, -1L);
			VidLib.LOGGER.info("Loaded data overrides");
		}

		if (dataRecorder != null && dataRecorder.start == -1L) {
			serverDataMap.overrides = dataRecorder.serverData;

			for (var entry : dataRecorder.playerData.entrySet()) {
				getClientSessionData(entry.getKey()).dataMap.overrides = entry.getValue();
			}
		}

		level.vl$preTick(paused);
		filteredZones.tick(level);

		updateOverrides(player);
		input = VLLocalPlayer.fromInput(window.getWindow(), player, mc.screen == null && mc.isWindowActive());

		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChanged(player, prevInput, input));
			prevInput = input;
			mc.c2s(new SyncPlayerInputToServer(input));
		}

		for (var otherPlayer : level.players()) {
			if (otherPlayer instanceof RemotePlayer p) {
				p.vl$sessionData().preTick(mc, level, p);
			}
		}

		if (mc.options.keyJump.isDown() && PlayerActionHandler.handle(player, PlayerActionType.JUMP, true)) {
			mc.options.keyJump.release();
		}

		if (mc.options.keyShift.isDown() && !Screen.hasAltDown() && PlayerActionHandler.handle(player, PlayerActionType.SNEAK, true)) {
			mc.options.keyShift.release();
		}

		if (mc.options.keySprint.isDown() && PlayerActionHandler.handle(player, PlayerActionType.SPRINT, true)) {
			mc.options.keySprint.release();
		}
	}

	@ApiStatus.Internal
	public void postTick(ClientLevel level, @Nullable LocalPlayer player, PauseType paused) {
		if (scheduledTaskHandler != null && paused.tick()) {
			scheduledTaskHandler.tick();
		}

		if (cameraOverride instanceof ClientCutscene cutscene && cutscene.tick()) {
			mc.stopCutscene();
		}

		if (cameraOverride instanceof ControlledCameraOverride c && c.tick()) {
			cameraOverride = null;
		}

		if (screenFade != null && screenFade.tick()) {
			screenFade = null;
		}

		if (!terrainHighlights.isEmpty() && paused.tick()) {
			terrainHighlights.removeIf(TerrainHighlightInstance::tick);
		}

		prevCameraShake = cameraShake;
		double shakeX = 0D;
		double shakeY = 0D;

		if (!screenShakeInstances.isEmpty()) {
			var shakeIt = screenShakeInstances.iterator();

			while (shakeIt.hasNext()) {
				var instance = shakeIt.next();
				var vec = instance.shake.type().get(instance.progress);
				var intensity = instance.shake.intensity();
				var intensityScale = instance.shake.start().easeMirrored(instance.ticks / (float) instance.shake.duration(), instance.shake.end());
				shakeX += vec.x() * intensity * intensityScale;
				shakeY += vec.y() * intensity * intensityScale;

				instance.progress += instance.shake.speed();

				if (++instance.ticks >= instance.shake.duration()) {
					shakeIt.remove();
				}
			}
		}

		cameraShake = Math.abs(shakeX) <= 0.0001D && Math.abs(shakeY) <= 0.0001D ? Identity.DVEC_2 : new Vector2d(shakeX, shakeY);

		if (paused.tick()) {
			tick++;

			for (var session : remoteSessionData.values()) {
				session.tick++;
			}
		}

		int undo = level.undoAllFutureModifications();

		if (undo > 0) {
			VidLib.LOGGER.info("Undone " + undo + " future modifications");
		}
	}

	public void refreshZones(ResourceKey<Level> dimension) {
		filteredZones.filter(dimension, serverZones);
		cachedZoneShapes = null;
		NeoForge.EVENT_BUS.post(new ZoneEvent.Updated(dimension, filteredZones, Side.CLIENT));
	}

	@Override
	public <V> void syncRegistry(Player player, SyncedRegistry<V> registry, Map<ResourceLocation, V> map) {
		registry.registry().update(map);

		if (registry.callback() != null) {
			registry.callback().run(player);
		}
	}

	@Override
	public void updateZones(Level level) {
		serverZones.update(ZoneContainer.REGISTRY.getMap().values());
		refreshZones(level.dimension());
	}

	@Override
	public void updateClocks(Map<ResourceLocation, ClockValue> map) {
		clocks.clear();
		clocks.putAll(map);
	}

	public DataRecorder initDataRecorder(Player player, long start) {
		if (dataRecorder == null) {
			dataRecorder = new DataRecorder(start != -1L, start);

			dataRecorder.load(
				player.level().registryAccess().createSerializationContext(JsonOps.INSTANCE),
				FMLPaths.GAMEDIR.get().resolve(start == -1L ? "replay_data_overrides.json" : ("replay_data_" + Long.toUnsignedString(start) + ".json"))
			);
		}

		return dataRecorder;
	}

	@Override
	public void updateServerData(long gameTime, Player self, List<DataMapValue> update) {
		serverDataMap.update(mc.player, update);

		if (VidLibConfig.debugS2CPackets) {
			var r = initDataRecorder(self, gameTime);

			if (r.record) {
				for (var u : update) {
					if (!u.key().skipLogging()) {
						r.setServer(gameTime, u.key(), u.value());
					}
				}
			}
		}
	}

	@Override
	public void updatePlayerData(long gameTime, Player self, UUID player, List<DataMapValue> update) {
		if (self.getUUID().equals(player)) {
			dataMap.update(self, update);
		} else {
			getRemoteSessionData(player).dataMap.update(self.level().getPlayerByUUID(player), update);
		}

		if (VidLibConfig.debugS2CPackets) {
			var r = initDataRecorder(self, gameTime);

			if (r.record) {
				for (var u : update) {
					if (!u.key().skipLogging()) {
						r.setPlayer(gameTime, player, u.key(), u.value());
					}
				}
			}
		}
	}

	@Override
	public void updatePlayerTags(long gameTime, Player self, UUID player, List<String> update) {
		var t = getClientSessionData(player).tags;
		t.clear();
		t.addAll(update);
		refreshListedPlayers();

		if (VidLibConfig.debugS2CPackets) {
			var r = initDataRecorder(self, gameTime);

			if (r.record) {
				r.setPlayer(gameTime, player, DataRecorder.PLAYER_TAGS, Set.copyOf(update));
			}
		}
	}

	@Override
	public void removeSessionData(UUID id) {
		// remoteSessionData.remove(id);
	}

	@Override
	public void refreshBlockZones() {
		cachedZoneShapes = null;
	}

	@Override
	public void updateInput(Level level, UUID player, PlayerInput input) {
		var data = getRemoteSessionData(player);
		data.prevInput = data.input = input;

		var entity = level.getEntityByUUID(player);

		if (entity != null) {
			var vehicle = entity.getVehicle();

			if (vehicle != null) {
				vehicle.vl$setPilotInput(input);
			}
		}
	}

	@Override
	public void updateSkyboxes() {
		skyboxes.clear();
	}

	@Override
	public void refreshListedPlayers() {
		originalListedPlayers = null;
	}

	public List<PlayerInfo> getListedPlayers() {
		if (originalListedPlayers == null) {
			var original = mc.player.connection.getListedOnlinePlayers().stream().sorted(PlayerTabOverlay.PLAYER_COMPARATOR).limit(80L).toList();
			originalListedPlayers = new ArrayList<>(original);

			return originalListedPlayers;
		}

		var listedPlayers = new ArrayList<PlayerInfo>(originalListedPlayers.size());

		for (var player : originalListedPlayers) {
			var data = getClientSessionData(player.getProfile().getId());

			if (!data.nameHidden) {
				listedPlayers.add(player);
			}
		}

		return listedPlayers;
	}

	public void startNPCRecording(Minecraft mc, GameProfile profile) {
		if (npcRecording == null) {
			npcRecording = new NPCRecording(profile);
			npcRecording.record(npcRecording.start, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true), mc.player);
		} else {
			mc.tell("Already recording NPC '" + profile.getName() + "'!");
		}
	}

	public void stopNPCRecording(Minecraft mc) {
		if (npcRecording != null) {
			npcRecording.length = System.currentTimeMillis() - npcRecording.start;

			var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), mc.level.registryAccess(), ConnectionType.NEOFORGE);
			var path = FMLPaths.GAMEDIR.get().resolve("vidlib/npc/" + npcRecording.start + "_" + npcRecording.profile.getName().toLowerCase(Locale.ROOT) + ".npcrec");

			if (Files.notExists(path.getParent())) {
				try {
					Files.createDirectories(path.getParent());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			try (var out = new BufferedOutputStream(Files.newOutputStream(path))) {
				npcRecording.write(buf);
				buf.readBytes(out, buf.readableBytes());
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			npcRecording = null;
			NPCRecording.REPLAY = null;
			mc.tell("NPC recording '" + path.getFileName() + "' saved!");
		}
	}

	public void replayNPCRecording(Minecraft mc) {
		var map = NPCRecording.getReplay(mc.level.registryAccess());

		if (map.isEmpty()) {
			return;
		}

		var last = map.lastEntry();
		mc.level.addParticle(new NPCParticleOptions(last.getKey(), false, 0, Optional.empty()), true, true, mc.player.getX(), mc.player.getY(), mc.player.getZ(), 0D, 0D, 0D);
	}
}
