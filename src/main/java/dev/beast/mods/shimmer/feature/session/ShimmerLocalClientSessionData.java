package dev.beast.mods.shimmer.feature.session;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.GameEventHandler;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.ShimmerConfig;
import dev.beast.mods.shimmer.core.ShimmerLocalPlayer;
import dev.beast.mods.shimmer.feature.camera.CameraShakeInstance;
import dev.beast.mods.shimmer.feature.camera.ControlledCameraOverride;
import dev.beast.mods.shimmer.feature.clock.ClockValue;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataRecorder;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.fade.ScreenFadeInstance;
import dev.beast.mods.shimmer.feature.input.PlayerInput;
import dev.beast.mods.shimmer.feature.input.PlayerInputChanged;
import dev.beast.mods.shimmer.feature.input.SyncPlayerInputToServer;
import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import dev.beast.mods.shimmer.feature.skybox.Skybox;
import dev.beast.mods.shimmer.feature.skybox.SkyboxData;
import dev.beast.mods.shimmer.feature.skybox.Skyboxes;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.ZoneClipResult;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneEvent;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import dev.beast.mods.shimmer.util.FrameInfo;
import dev.beast.mods.shimmer.util.PauseType;
import dev.beast.mods.shimmer.util.ScheduledTask;
import dev.beast.mods.shimmer.util.Side;
import dev.beast.mods.shimmer.util.registry.SyncedRegistry;
import dev.latvian.mods.kmath.Range;
import dev.latvian.mods.kmath.Vec2d;
import dev.latvian.mods.kmath.VoxelShapeBox;
import dev.latvian.mods.kmath.WorldMouse;
import dev.latvian.mods.kmath.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ShimmerLocalClientSessionData extends ShimmerClientSessionData {
	public final Minecraft mc;
	public final ClientPacketListener connection;
	private final Map<UUID, ShimmerRemoteClientSessionData> remoteSessionData;
	private ScheduledTask.Handler scheduledTaskHandler;
	public final ActiveZones serverZones;
	public final ActiveZones filteredZones;
	public ZoneClipResult zoneClip;
	public final List<CameraShakeInstance> cameraShakeInstances;
	public Vec2d prevCameraShake;
	public Vec2d cameraShake;
	public Map<ResourceLocation, ClockValue> clocks;
	public Map<ResourceLocation, Skybox> skyboxes;
	public final DataMap serverDataMap;
	public Skybox skybox;
	public Map<ZoneShape, VoxelShapeBox> cachedZoneShapes;
	public List<PlayerInfo> originalListedPlayers;
	public CameraOverride cameraOverride;
	public ScreenFadeInstance screenFade;
	public FrameInfo currentFrameInfo;
	public WorldMouse worldMouse;
	public DataRecorder dataRecorder;

	public ShimmerLocalClientSessionData(Minecraft mc, UUID uuid, ClientPacketListener connection) {
		super(uuid);
		this.mc = mc;
		this.connection = connection;
		this.remoteSessionData = new HashMap<>();

		this.serverZones = new ActiveZones();
		this.filteredZones = new ActiveZones();
		this.zoneClip = null;
		this.cameraShakeInstances = new ArrayList<>();
		this.prevCameraShake = this.cameraShake = Vec2d.ZERO;
		this.clocks = new HashMap<>();
		this.skyboxes = new HashMap<>();
		this.serverDataMap = new DataMap(uuid, DataType.SERVER);
		this.skybox = null;
		Shimmer.LOGGER.info("Client Session Data Initialized");
	}

	public ShimmerRemoteClientSessionData getRemoteSessionData(UUID id) {
		var data = remoteSessionData.get(id);

		if (data == null) {
			data = new ShimmerRemoteClientSessionData(id);
			remoteSessionData.put(id, data);
		}

		return data;
	}

	public ShimmerClientSessionData getClientSessionData(UUID id) {
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

		if (f == null) {
			MiscShimmerClientUtils.fogOverride = FogParameters.NO_FOG;
		} else if (f.color().argb() == 0) {
			if (f.shape() == 0) {
				MiscShimmerClientUtils.fogOverride = FogParameters.NO_FOG;
			} else {
				MiscShimmerClientUtils.fogOverride = null;
			}
		} else {
			var shape = f.shape() == 0 ? FogShape.SPHERE : FogShape.CYLINDER;
			var c = f.color();
			MiscShimmerClientUtils.fogOverride = new FogParameters(f.range().min(), f.range().max(), shape, c.redf(), c.greenf(), c.bluef(), c.alphaf());
		}
	}

	@Override
	public void respawned(Level level, boolean loggedIn) {
		refreshZones(level.dimension());
	}

	@ApiStatus.Internal
	public void preTick(ClientLevel level, LocalPlayer player, Window window, PauseType paused) {
		if (dataRecorder == null && !ShimmerConfig.debugS2CPackets && player.isReplayCamera()) {
			dataRecorder = initDataRecorder(player, -1L);
			Shimmer.LOGGER.info("Loaded data overrides");
		}

		if (dataRecorder != null && dataRecorder.start == -1L) {
			serverDataMap.overrides = dataRecorder.serverData;

			for (var entry : dataRecorder.playerData.entrySet()) {
				getClientSessionData(entry.getKey()).dataMap.overrides = entry.getValue();
			}
		}

		filteredZones.tick(level);

		updateOverrides(player);
		input = ShimmerLocalPlayer.fromInput(window.getWindow(), player, mc.screen == null && mc.isWindowActive());

		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChanged(player, prevInput, input));
			prevInput = input;
			mc.c2s(new SyncPlayerInputToServer(input));
		}

		for (var otherPlayer : level.players()) {
			if (otherPlayer instanceof RemotePlayer p) {
				p.shimmer$sessionData().preTick(mc, level, p);
			}
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

		prevCameraShake = cameraShake;
		double shakeX = 0D;
		double shakeY = 0D;

		if (!cameraShakeInstances.isEmpty()) {
			var shakeIt = cameraShakeInstances.iterator();

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

		cameraShake = Math.abs(shakeX) <= 0.0001D && Math.abs(shakeY) <= 0.0001D ? Vec2d.ZERO : new Vec2d(shakeX, shakeY);

		if (paused.tick()) {
			tick++;

			for (var session : remoteSessionData.values()) {
				session.tick++;
			}
		}

		int undo = level.undoAllFutureModifications();

		if (undo > 0) {
			Shimmer.LOGGER.info("Undone " + undo + " future modifications");
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

		if (ShimmerConfig.debugS2CPackets) {
			var r = initDataRecorder(self, gameTime);

			if (r.record) {
				for (var u : update) {
					if (!u.type().skipLogging()) {
						r.setServer(gameTime, u.type(), u.value());
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

		if (ShimmerConfig.debugS2CPackets) {
			var r = initDataRecorder(self, gameTime);

			if (r.record) {
				for (var u : update) {
					if (!u.type().skipLogging()) {
						r.setPlayer(gameTime, player, u.type(), u.value());
					}
				}
			}
		}
	}

	@Override
	public void removeSessionData(UUID id) {
		// remoteSessionData.remove(id);
	}

	@Override
	public void updatePlayerTags(long gameTime, Player self, UUID player, List<String> update) {
		var t = getClientSessionData(player).tags;
		t.clear();
		t.addAll(update);
		refreshListedPlayers();

		if (ShimmerConfig.debugS2CPackets) {
			var r = initDataRecorder(self, gameTime);

			if (r.record) {
				r.setPlayer(gameTime, player, DataRecorder.PLAYER_TAGS, Set.copyOf(update));
			}
		}
	}

	@Override
	public void refreshBlockZones() {
		cachedZoneShapes = null;
	}

	@Override
	public void updateInput(UUID player, PlayerInput input) {
		var data = getRemoteSessionData(player);
		data.prevInput = data.input = input;
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
}
