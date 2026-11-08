package dev.latvian.mods.vidlib.feature.session;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.net.S2CPacketBundleBuilder;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.data.InternalGlobalData;
import dev.latvian.mods.vidlib.feature.data.SyncLevelDataPayload;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

public class ServerSessionData extends GlobalSessionData {
	public static ServerSessionData of(MinecraftServer server) {
		return VLPlatformHelper.INSTANCE.getServerWorldData(server);
	}

	public final MinecraftServer server;
	public final RandomSource random;
	public final UUID sessionId;
	public final Map<UUID, ServerPlayerSessionData> serverSessionData;
	public PacketCapture packetCapture;

	public ServerSessionData(MinecraftServer server) {
		super(server.registryAccess());
		this.server = server;
		this.random = RandomSource.create();
		this.sessionId = UUID.randomUUID();
		this.serverSessionData = new Object2ObjectOpenHashMap<>();

		if (server.getClass().getName().contains("ReplayServer")) {
			isReplay = true;
		}

		dataMap.load(server, server.getWorldPath(LevelResource.ROOT).resolve("vidlib.nbt"));
	}

	@Override
	public long getLevelGameTime() {
		return server.overworld().getGameTime();
	}

	@Override
	public PauseType getPauseType() {
		return server.isPaused() ? PauseType.GAME : server.tickRateManager().runsNormally() ? PauseType.NONE : PauseType.TICK;
	}

	@Override
	public FeatureSet getServerFeatures() {
		return FeatureSet.SERVER_FEATURES.get();
	}

	@Override
	public ServerLevelSessionData getLevel(ResourceKey<Level> dimension) {
		return (ServerLevelSessionData) super.getLevel(dimension);
	}

	@Override
	protected ServerLevelSessionData createLevelData(ResourceKey<Level> dimension) {
		return new ServerLevelSessionData(this, dimension, server.getLevel(dimension));
	}

	@Override
	public ServerPlayerSessionData getPlayer(UUID uuid) {
		return (ServerPlayerSessionData) super.getPlayer(uuid);
	}

	@Override
	protected ServerPlayerSessionData createPlayerData(UUID uuid) {
		var session = new ServerPlayerSessionData(this, server, uuid);
		session.load();
		return session;
	}

	@Override
	public void preTick(PauseType pauseType) {
		super.preTick(pauseType);

		for (var player : server.getPlayerList().getPlayers()) {
			ServerPlayerSessionData.of(player).preTick(player);
		}

		for (var level : server.getAllLevels()) {
			ServerLevelSessionData.of(level).tick(level, pauseType);
		}
	}

	@Override
	public void postTick(PauseType pauseType) {
		super.postTick(pauseType);

		if (isReplay) {
			return;
		}

		var packetsToEveryone = new S2CPacketBundleBuilder();

		dataMap.sync(packetsToEveryone, SyncServerDataPayload::new);

		for (var level : levelMap.values()) {
			level.dataMap.sync(packetsToEveryone, SyncLevelDataPayload::new);
		}

		for (var session : playerMap.values()) {
			session.dataMap.sync(packetsToEveryone, SyncPlayerDataPayload::new);
		}

		for (var player : server.getPlayerList().getPlayers()) {
			ServerPlayerSessionData.of(player).syncPlayer(player, packetsToEveryone);
		}

		packetsToEveryone.send(server);

		int minFakeLag0 = dataMap.get(InternalGlobalData.MIN_FAKE_LAG);
		int maxFakeLag0 = dataMap.get(InternalGlobalData.MAX_FAKE_LAG);
		int minFakeLag = Math.min(minFakeLag0, maxFakeLag0);
		int maxFakeLag = Math.max(minFakeLag0, maxFakeLag0);

		if (maxFakeLag > 0 && minFakeLag >= 0) {
			try {
				Thread.sleep(Mth.ceil(random.nextIntBetweenInclusive(minFakeLag, maxFakeLag)));
			} catch (Exception ignore) {
			}
		}

		HubServerGateway.tickGateway();
	}

	public void eraseServerSession(UUID uuid) {
		var player = server.getPlayerList().getPlayer(uuid);

		if (player != null) {
			player.connection.disconnect(Component.literal("Your player data was reset"));
		}

		schedule(1, () -> {
			var data = serverSessionData.remove(uuid);

			if (data != null) {
				data.setOnline(false);
				server.s2c(new RemovePlayerDataPayload(uuid));

				try {
					Files.deleteIfExists(data.dataMapPath);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	@Nullable
	public PacketCapture startPacketCapture() {
		if (packetCapture == null && !CommonGameEngine.INSTANCE.disablePacketCapture()) {
			var directory = VidLibPaths.LOCAL.get().resolve("packet-capture");

			if (Files.notExists(directory)) {
				try {
					Files.createDirectories(directory);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			packetCapture = new PacketCapture(server, sessionId, directory);
		}

		return packetCapture;
	}

	public void save() {
		dataMap.save(server, server.getWorldPath(LevelResource.ROOT).resolve("vidlib.nbt"));

		if (packetCapture != null) {
			packetCapture.saveAll();
		}
	}

	public void preloadAllSessions() {
		var dir = VLPlatformHelper.INSTANCE.getPlayerDataDirectory(server);

		if (Files.notExists(dir)) {
			return;
		}

		try (var stream = Files.list(dir)) {
			for (var path : stream.filter(Files::isRegularFile).toList()) {
				var name = path.getFileName().toString();

				if (name.endsWith(".nbt")) {
					var uuid = UndashedUuid.fromStringLenient(name.substring(0, name.length() - 4));
					getPlayer(uuid);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void stopped() {
		if (packetCapture != null) {
			packetCapture.finish();
		}

		HubServerGateway.stopGateway();
	}

	public void playerJoined(ServerPlayer player) {
		PlayerProfiles.cache(player.getGameProfile());
		var session = PlayerSessionData.of(player);
		session.setOnline(true);
		session.setName(player.getScoreboardName());
		VidLib.sync(player, 2);
	}

	public void playerLeft(ServerPlayer player) {
		var session = PlayerSessionData.of(player);
		session.setOnline(false);
	}
}
