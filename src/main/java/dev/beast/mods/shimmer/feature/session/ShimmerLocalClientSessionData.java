package dev.beast.mods.shimmer.feature.session;

import com.mojang.blaze3d.platform.Window;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerLocalPlayer;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.input.PlayerInput;
import dev.beast.mods.shimmer.feature.input.PlayerInputChanged;
import dev.beast.mods.shimmer.feature.input.SyncPlayerInputToServer;
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
import dev.beast.mods.shimmer.math.VoxelShapeBox;
import dev.beast.mods.shimmer.util.Side;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShimmerLocalClientSessionData extends ShimmerClientSessionData {
	public final ClientPacketListener connection;
	public final ActiveZones serverZones;
	public final ActiveZones filteredZones;
	private final Map<UUID, ShimmerRemoteClientSessionData> remoteSessionData;
	public final Set<String> tags;
	public ZoneClipResult zoneClip;
	public Map<ResourceLocation, ClockFont> clockFonts;
	public Map<ResourceLocation, ClockInstance> clocks;
	public final DataMap serverDataMap;
	public Map<ZoneShape, VoxelShapeBox> cachedZoneShapes;

	public ShimmerLocalClientSessionData(UUID uuid, ClientPacketListener connection) {
		super(uuid);
		this.connection = connection;
		Shimmer.LOGGER.info("Client Session Data Initialized");
		this.serverZones = new ActiveZones();
		this.filteredZones = new ActiveZones();
		this.remoteSessionData = new HashMap<>();
		this.tags = new HashSet<>(0);
		this.clockFonts = Map.of();
		this.clocks = Map.of();
		this.serverDataMap = new DataMap(uuid, DataType.SERVER);
	}

	public ShimmerRemoteClientSessionData getRemoteSessionData(UUID id) {
		var data = remoteSessionData.get(id);

		if (data == null) {
			data = new ShimmerRemoteClientSessionData(id);
			remoteSessionData.put(id, data);
		}

		return data;
	}

	@Override
	public void respawned(Level level, boolean loggedIn) {
		refreshZones(level.dimension());
	}

	@Override
	public void closed() {
		ClockFont.CLIENT_SUPPLIER = Map::of;
	}

	@ApiStatus.Internal
	public void preTick(Minecraft mc, ClientLevel level, LocalPlayer player, Window window, boolean paused) {
		if (paused) {
			return;
		}

		filteredZones.entityZones.clear();

		for (var container : filteredZones) {
			container.tick(filteredZones, level);
		}

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
	public void updateZones(Level level, List<ZoneContainer> update) {
		serverZones.update(update);
		NeoForge.EVENT_BUS.post(new ZoneEvent.AllUpdated(serverZones, Side.SERVER));
		refreshZones(level.dimension());
	}

	@Override
	public void updateClockFonts(List<ClockFont> update) {
		var map = new HashMap<ResourceLocation, ClockFont>();

		for (var font : update) {
			map.put(font.id(), font);
		}

		clockFonts = Map.copyOf(map);
		ClockFont.CLIENT_SUPPLIER = () -> clockFonts;
	}

	@Override
	public void updateClocks(Level level, List<ClockInstance> update) {
		var map = new HashMap<ResourceLocation, ClockInstance>();

		for (var clock : update) {
			map.put(clock.clock.id(), clock);
		}

		clocks = Map.copyOf(map);
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
		var t = uuid.equals(player) ? tags : getRemoteSessionData(player).tags;
		t.clear();
		t.addAll(update);
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
}
