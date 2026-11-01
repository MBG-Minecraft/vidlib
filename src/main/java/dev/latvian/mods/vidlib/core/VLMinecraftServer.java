package dev.latvian.mods.vidlib.core;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.klib.net.S2CPacketBundleBuilder;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.ZoneCache;
import dev.latvian.mods.vidlib.math.knumber.SyncGlobalNumberVariablesPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface VLMinecraftServer extends VLMinecraftEnvironment {
	default MinecraftServer vl$self() {
		return (MinecraftServer) this;
	}

	@Override
	default boolean isClient() {
		return false;
	}

	@Override
	default ServerLevel vl$level() {
		return vl$self().overworld();
	}

	default RandomSource vl$sessionRandom() {
		throw new NoMixinException(this);
	}

	default int vl$sessionId() {
		throw new NoMixinException(this);
	}

	@Override
	default PauseType getPauseType() {
		var server = vl$self();
		return server.isPaused() ? PauseType.GAME : server.tickRateManager().runsNormally() ? PauseType.NONE : PauseType.TICK;
	}

	@Override
	default FeatureSet getServerFeatures() {
		return FeatureSet.SERVER_FEATURES.get();
	}

	@Override
	default List<? extends Player> vl$getS2CPlayers() {
		return vl$self().getPlayerList().getPlayers();
	}

	@Override
	default Collection<ServerSessionData> vl$getAllSessionData() {
		return List.of();
	}

	@ApiStatus.Internal
	default void vl$playerJoined(ServerPlayer player) {
		PlayerProfiles.cache(player.getGameProfile());
		player.vl$sessionData().dataMap.set(InternalPlayerData.ONLINE, true);
		player.vl$sessionData().dataMap.set(InternalPlayerData.NAME, player.getScoreboardName());
		VidLib.sync(player, 2);
	}

	@ApiStatus.Internal
	default void vl$playerLeft(ServerPlayer player) {
		player.vl$sessionData().dataMap.set(InternalPlayerData.ONLINE, false);
	}

	@Override
	@ApiStatus.Internal
	default void vl$preTick(PauseType paused) {
		for (var level : vl$self().getAllLevels()) {
			level.vl$preTick(paused);
			ZoneCache.of(level.dimension(), false).tick(level);
		}

		for (var player : vl$self().getPlayerList().getPlayers()) {
			player.vl$sessionData().vl$preTick(player);
		}
	}

	@Override
	@ApiStatus.Internal
	default void vl$postTick(PauseType paused) {
		if (VLPlatformHelper.CURRENT.isReplayLevel(vl$level())) {
			return;
		}

		var packetsToEveryone = new S2CPacketBundleBuilder();

		if (!vl$isReplayServer()) {
			getDataMap().sync(packetsToEveryone, (playerId, update) -> new SyncServerDataPayload(update));

			for (var session : vl$self().vl$getAllSessionData()) {
				session.dataMap.sync(packetsToEveryone, SyncPlayerDataPayload::new);
			}
		}

		for (var player : vl$self().getPlayerList().getPlayers()) {
			player.vl$sessionData().syncPlayer(player, packetsToEveryone);
		}

		for (var session : vl$self().vl$getAllSessionData()) {
			session.tick++;
		}

		packetsToEveryone.send(this);

		int minFakeLag0 = get(InternalServerData.MIN_FAKE_LAG);
		int maxFakeLag0 = get(InternalServerData.MAX_FAKE_LAG);
		int minFakeLag = Math.min(minFakeLag0, maxFakeLag0);
		int maxFakeLag = Math.max(minFakeLag0, maxFakeLag0);

		if (maxFakeLag > 0 && minFakeLag >= 0) {
			try {
				Thread.sleep(Mth.ceil(vl$level().getRandom().nextIntBetweenInclusive(minFakeLag, maxFakeLag)));
			} catch (Exception ignore) {
			}
		}
	}

	@Override
	default void setAnchor(Anchor anchor) {
		VLMinecraftEnvironment.super.setAnchor(anchor);

		for (var level : vl$self().getAllLevels()) {
			level.vl$updateLoadedChunks();
		}
	}

	@Override
	default void syncGlobalVariables() {
		s2c(new SyncGlobalNumberVariablesPayload(globalVariables()));
	}

	@Override
	default String getServerBrand() {
		return "neoforge";
	}

	default void runServerCommand(String command) {
		vl$self().getCommands().performPrefixedCommand(vl$self().createCommandSourceStack(), command);
	}

	@Nullable
	default PacketCapture vl$getPacketCapture(boolean start) {
		throw new NoMixinException(this);
	}

	default void vl$save() {
		getDataMap().save(vl$self(), vl$self().getWorldPath(LevelResource.ROOT).resolve("vidlib.nbt"));

		var packetCapture = vl$getPacketCapture(false);

		if (packetCapture != null) {
			packetCapture.saveAll();
		}
	}

	default ServerSessionData vl$getOrLoadServerSession(UUID uuid) {
		throw new NoMixinException(this);
	}

	default void vl$eraseServerSession(UUID uuid) {
		throw new NoMixinException(this);
	}

	default void vl$preloadAllSessions() {
		var dir = VLPlatformHelper.CURRENT.getPlayerDataDirectory(vl$self());

		if (Files.notExists(dir)) {
			return;
		}

		try (var stream = Files.list(dir)) {
			for (var path : stream.filter(Files::isRegularFile).toList()) {
				var name = path.getFileName().toString();

				if (name.endsWith(".nbt")) {
					var uuid = UndashedUuid.fromStringLenient(name.substring(0, name.length() - 4));
					vl$getOrLoadServerSession(uuid);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	default boolean vl$isReplayServer() {
		return false;
	}
}
