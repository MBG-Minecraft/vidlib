package dev.beast.mods.shimmer.feature.session;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.FogShape;
import dev.beast.mods.shimmer.GameEventHandler;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerLocalPlayer;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.input.PlayerInput;
import dev.beast.mods.shimmer.feature.input.PlayerInputChanged;
import dev.beast.mods.shimmer.feature.input.SyncPlayerInputToServer;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import dev.beast.mods.shimmer.feature.skybox.Skybox;
import dev.beast.mods.shimmer.feature.skybox.SkyboxData;
import dev.beast.mods.shimmer.feature.skybox.Skyboxes;
import dev.beast.mods.shimmer.feature.worldsync.ProgressingText;
import dev.beast.mods.shimmer.feature.worldsync.WorldSync;
import dev.beast.mods.shimmer.feature.worldsync.WorldSyncAuthResponsePayload;
import dev.beast.mods.shimmer.feature.worldsync.WorldSyncReadThread;
import dev.beast.mods.shimmer.feature.worldsync.WorldSyncRepo;
import dev.beast.mods.shimmer.feature.worldsync.WorldSyncScreen;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.ZoneClipResult;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneEvent;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.Range;
import dev.beast.mods.shimmer.math.VoxelShapeBox;
import dev.beast.mods.shimmer.util.PauseType;
import dev.beast.mods.shimmer.util.Side;
import dev.beast.mods.shimmer.util.registry.SyncedRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShimmerLocalClientSessionData extends ShimmerClientSessionData {
	public final ClientPacketListener connection;
	public final ActiveZones serverZones;
	public final ActiveZones filteredZones;
	private final Map<UUID, ShimmerRemoteClientSessionData> remoteSessionData;
	public ZoneClipResult zoneClip;
	public Map<ResourceLocation, ClockInstance> clocks;
	public Map<ResourceLocation, Skybox> skyboxes;
	public final DataMap serverDataMap;
	public Skybox skybox;
	public Map<ZoneShape, VoxelShapeBox> cachedZoneShapes;
	public List<PlayerInfo> originalListedPlayers;

	public ShimmerLocalClientSessionData(UUID uuid, ClientPacketListener connection) {
		super(uuid);
		this.connection = connection;
		Shimmer.LOGGER.info("Client Session Data Initialized");
		this.serverZones = new ActiveZones();
		this.filteredZones = new ActiveZones();
		this.remoteSessionData = new HashMap<>();
		this.clocks = Map.of();
		this.skyboxes = new HashMap<>();
		this.serverDataMap = new DataMap(uuid, DataType.SERVER);
		this.skybox = null;
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

	@Override
	public void updateOverrides(Player player) {
		super.updateOverrides(player);
		var skyboxId = EntityOverride.SKYBOX.get(player);

		if (skyboxId == null) {
			skyboxId = player.level().getSkybox();
		}

		if (skyboxId == null || skyboxId.equals(Skyboxes.DEFAULT)) {
			skybox = null;
		} else {
			skybox = skyboxes.get(skyboxId);

			if (skybox == null) {
				var skyboxData = SkyboxData.REGISTRY.get(skyboxId);

				if (skyboxData == null) {
					skyboxData = new SkyboxData(skyboxId, Optional.empty(), 0F, 0F, Color.WHITE, false);
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

	@Override
	public void closed() {
	}

	@ApiStatus.Internal
	public void preTick(Minecraft mc, ClientLevel level, LocalPlayer player, Window window, PauseType paused) {
		if (!paused.tick()) {
			return;
		}

		filteredZones.tick(level);

		for (var instance : clocks.values()) {
			if (instance.clock.dimension() == level.dimension()) {
				instance.tick(level);
			}
		}

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
	public void updateClockInstance(ResourceLocation id, int tick, boolean ticking) {
		var instance = clocks.get(id);

		if (instance != null) {
			instance.tick = instance.prevTick = tick;
			instance.ticking = ticking;
		}
	}

	@Override
	public void updateSessionData(Player self, UUID player, List<DataMapValue> playerData) {
		if (self.getUUID().equals(player)) {
			dataMap.update(self, playerData);
		} else {
			getRemoteSessionData(player).dataMap.update(self.level().getPlayerByUUID(player), playerData);
		}
	}

	@Override
	public void removeSessionData(UUID id) {
		remoteSessionData.remove(id);
	}

	@Override
	public void updatePlayerTags(UUID player, List<String> update) {
		var t = getClientSessionData(player).tags;
		t.clear();
		t.addAll(update);
		refreshListedPlayers();
	}

	@Override
	public void updateServerData(List<DataMapValue> serverData) {
		serverDataMap.update(null, serverData);
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
	public void worldSyncAuthResponse(WorldSyncAuthResponsePayload payload) {
		var mc = Minecraft.getInstance();

		if (payload.token().isEmpty()) {
			mc.player.displayClientMessage(Component.literal("Failed to auth on WorldSync!").withStyle(ChatFormatting.RED), false);
			return;
		}

		var ip = payload.address();

		if (ip.equals("${ip}")) {
			var info = mc.getCurrentServer();
			ip = info == null ? "localhost" : info.ip;
			int ipp = ip.indexOf(':');

			if (ipp != -1) {
				ip = ip.substring(0, ipp);
			}
		}

		var ip0 = ip;

		Thread.startVirtualThread(() -> {
			mc.execute(() -> mc.player.displayClientMessage(Component.literal("Connecting to WorldSync repository ").append(Component.literal(payload.id()).withStyle(ChatFormatting.YELLOW)).append("..."), false));

			boolean https = false;

			try {
				if (WorldSync.HTTP_CLIENT.send(HttpRequest.newBuilder().uri(URI.create("https://" + ip0 + ":" + payload.port()))
					.GET()
					.timeout(Duration.ofSeconds(5L))
					.build(), HttpResponse.BodyHandlers.discarding()).statusCode() / 100 == 2) {
					https = true;
				}
			} catch (Exception ignore) {
			}

			if (!https) {
				boolean ok = false;

				try {
					if (WorldSync.HTTP_CLIENT.send(HttpRequest.newBuilder().uri(URI.create("http://" + ip0 + ":" + payload.port()))
						.GET()
						.timeout(Duration.ofSeconds(5L))
						.build(), HttpResponse.BodyHandlers.discarding()).statusCode() / 100 == 2) {
						ok = true;
					}
				} catch (Exception ignore) {
				}

				if (!ok) {
					mc.execute(() -> mc.player.displayClientMessage(Component.literal("Failed to connect to WorldSync repository ").append(Component.literal(payload.id()).withStyle(ChatFormatting.RED)), false));
					return;
				}
			}

			var repo = new WorldSyncRepo(payload.id(), payload.displayName(), (https ? "https://" : "http://") + ip0 + ":" + payload.port(), payload.token());
			WorldSyncRepo.MAP.get().values().removeIf(repo::replaces);
			WorldSyncRepo.MAP.get().put(repo.id(), repo);
			WorldSyncRepo.save();
			mc.execute(() -> mc.player.displayClientMessage(Component.literal("Added WorldSync repository ").append(Component.literal(repo.id()).withStyle(ChatFormatting.GREEN)).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/world-sync browse " + repo.id()))), false));
		});
	}

	public void prepareWorldSyncScreen(String ip, int port) {
		var mc = Minecraft.getInstance();

		if (ip.equals("${ip}")) {
			var info = mc.getCurrentServer();
			ip = info == null ? "localhost" : info.ip;
			int ipp = ip.indexOf(':');

			if (ipp != -1) {
				ip = ip.substring(0, ipp);
			}
		}

		var screen = new WorldSyncScreen();
		screen.text.add(new ProgressingText().setText("Indexing files..."));
		screen.thread = new WorldSyncReadThread(mc, screen, ip, port);
		mc.setScreen(screen);
	}

	public void startWorldSync() {
		if (Minecraft.getInstance().screen instanceof WorldSyncScreen screen) {
			screen.startThread();
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

	public List<PlayerInfo> getListedPlayers(Minecraft mc) {
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

	public Component modifyPlayerName(UUID id, Component original) {
		var prefix = getClientSessionData(id).namePrefix;

		if (prefix != null) {
			return Component.empty().append(prefix).append(original);
		}

		return original;
	}
}
