package dev.latvian.mods.vidlib.feature.session;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.net.S2CPacketBundleBuilder;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

public class ServerWorldSessionData extends WorldSessionData {
	public static ServerWorldSessionData of(MinecraftServer server) {
		return new ServerWorldSessionData(server);
	}

	public final MinecraftServer server;
	public final RandomSource random;
	public final UUID sessionId;
	public final Map<UUID, ServerPlayerSessionData> serverSessionData;
	public final boolean isReplayServer;
	public PacketCapture packetCapture;

	public ServerWorldSessionData(MinecraftServer server) {
		super(server.registryAccess());
		this.server = server;
		this.random = RandomSource.create();
		this.sessionId = UUID.randomUUID();
		this.serverSessionData = new Object2ObjectOpenHashMap<>();
		this.isReplayServer = server.getClass().getName().contains("ReplayServer");

		dataMap.load(server, server.getWorldPath(LevelResource.ROOT).resolve("vidlib.nbt"));
	}

	@Override
	public long getLevelGameTime() {
		return server.overworld().getGameTime();
	}

	@Override
	protected ServerLevelSessionData createLevelData(ResourceKey<Level> level) {
		return new ServerLevelSessionData(this, level);
	}

	@Override
	protected PlayerSessionData createPlayerData(UUID uuid) {
		var session = new ServerPlayerSessionData(this, server, uuid);
		session.load();
		return session;
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
	public void preTick(PauseType pauseType) {
		super.preTick(pauseType);

		for (var player : server.getPlayerList().getPlayers()) {
			player.vl$sessionData().preTick(player);
		}

		for (var level : server.getAllLevels()) {
			getLevel(level.dimension()).tick(level, pauseType);
		}
	}

	@Override
	public void postTick(PauseType pauseType) {
		super.postTick(pauseType);

		if (VLPlatformHelper.INSTANCE.isReplayLevel(server.overworld())) {
			return;
		}

		var packetsToEveryone = new S2CPacketBundleBuilder();

		if (!VLPlatformHelper.INSTANCE.isReplayServer(server)) {
			dataMap.sync(packetsToEveryone, (playerId, update) -> new SyncServerDataPayload(update));

			for (var session : server.vl$getAllSessionData()) {
				session.dataMap.sync(packetsToEveryone, SyncPlayerDataPayload::new);
			}
		}

		for (var player : server.getPlayerList().getPlayers()) {
			player.vl$sessionData().syncPlayer(player, packetsToEveryone);
		}

		packetsToEveryone.send(server);

		int minFakeLag0 = dataMap.get(InternalServerData.MIN_FAKE_LAG);
		int maxFakeLag0 = dataMap.get(InternalServerData.MAX_FAKE_LAG);
		int minFakeLag = Math.min(minFakeLag0, maxFakeLag0);
		int maxFakeLag = Math.max(minFakeLag0, maxFakeLag0);

		if (maxFakeLag > 0 && minFakeLag >= 0) {
			try {
				Thread.sleep(Mth.ceil(random.nextIntBetweenInclusive(minFakeLag, maxFakeLag)));
			} catch (Exception ignore) {
			}
		}
	}

	public void eraseServerSession(UUID uuid) {
		var player = server.getPlayerList().getPlayer(uuid);

		if (player != null) {
			player.connection.disconnect(Component.literal("Your player data was reset"));
		}

		schedule(1, () -> {
			var data = serverSessionData.remove(uuid);

			if (data != null) {
				data.dataMap.set(InternalPlayerData.ONLINE, false);
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
	public PacketCapture getPacketCapture(boolean start) {
		if (packetCapture == null && start && !CommonGameEngine.INSTANCE.disablePacketCapture()) {
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
}
