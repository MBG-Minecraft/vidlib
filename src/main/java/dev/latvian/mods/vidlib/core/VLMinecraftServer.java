package dev.latvian.mods.vidlib.core;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.clock.SyncClocksPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.misc.EventMarkerData;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.RemoveZonePayload;
import dev.latvian.mods.vidlib.feature.zone.UpdateZonePayload;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneLoader;
import dev.latvian.mods.vidlib.math.knumber.SyncGlobalNumberVariablesPayload;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.PauseType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
	default List<? extends Player> vl$getS2CPlayers() {
		return vl$self().getPlayerList().getPlayers();
	}

	@ApiStatus.Internal
	default void vl$playerJoined(ServerPlayer player) {
		VidLib.sync(player, 2);
		player.server.marker(new EventMarkerData("player/logged_in", player));
	}

	@Override
	@ApiStatus.Internal
	default void vl$preTick(PauseType paused) {
		for (var level : vl$self().getAllLevels()) {
			level.vl$preTick(paused);

			var zones = ZoneLoader.SERVER_BY_DIMENSION.get(level.dimension());
			level.vl$setActiveZones(zones);

			if (zones != null) {
				zones.tick(level);
			}
		}

		for (var player : vl$self().getPlayerList().getPlayers()) {
			player.vl$sessionData().vl$preTick(player);
		}
	}

	@Override
	@ApiStatus.Internal
	default void vl$postTick(PauseType paused) {
		if (vl$level().isReplayLevel()) {
			return;
		}

		var packetsToEveryone = new S2CPacketBundleBuilder(vl$level());

		getServerData().sync(packetsToEveryone, null, (playerId, update) -> new SyncServerDataPayload(update));

		for (var player : vl$self().getPlayerList().getPlayers()) {
			player.vl$sessionData().vl$postTick(packetsToEveryone, player);
		}

		packetsToEveryone.send(this);
	}

	default void betterDefaultGameRules() {
		var server = vl$self();
		server.overworld().setDayTime(6000L);
		server.overworld().setWeatherParameters(20000000, 20000000, false, false);
		server.setFlightAllowed(true);

		if (server.isSingleplayer()) {
			server.getPlayerList().setAllowCommandsForAllPlayers(true);
		}

		var rules = server.getGameRules();
		rules.getRule(GameRules.RULE_DOFIRETICK).set(false, server);
		rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, server);
		rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
		// rules.getRule(GameRules.RULE_DOBLOCKDROPS).set(false, server);
		rules.getRule(GameRules.RULE_COMMANDBLOCKOUTPUT).set(false, server);
		rules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, server);
		rules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);
		rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, server);
		rules.getRule(GameRules.RULE_SPAWN_RADIUS).set(0, server);
		rules.getRule(GameRules.RULE_DISABLE_PLAYER_MOVEMENT_CHECK).set(true, server);
		rules.getRule(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK).set(true, server);
		rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
		rules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(false, server);
		rules.getRule(GameRules.RULE_DISABLE_RAIDS).set(true, server);
		rules.getRule(GameRules.RULE_DOINSOMNIA).set(false, server);
		rules.getRule(GameRules.RULE_FALL_DAMAGE).set(false, server);
		rules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(false, server);
		rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, server);
		rules.getRule(GameRules.RULE_DO_WARDEN_SPAWNING).set(false, server);
		rules.getRule(GameRules.RULE_GLOBAL_SOUND_EVENTS).set(false, server);
	}

	default void setClock(ResourceLocation id, ClockValue value) {
		var map = vl$getClocks();

		if (!Objects.equals(map.put(id, value), value)) {
			s2c(new SyncClocksPayload(map));
		}
	}

	default void resetClock(ResourceLocation id) {
		var map = vl$getClocks();

		if (map.remove(id) != null) {
			s2c(new SyncClocksPayload(map));
		}
	}

	default void resetAllClocks() {
		var map = vl$getClocks();

		if (!map.isEmpty()) {
			map.clear();
			s2c(new SyncClocksPayload(map));
		}
	}

	default void setClock(ResourceLocation id, int second) {
		setClock(id, new ClockValue(second, second <= 0 ? ClockValue.Type.FINISHED : second <= 10 ? ClockValue.Type.FLASH : ClockValue.Type.NORMAL));
	}

	default void setClock(ResourceLocation id, int second, int maxSecond) {
		setClock(id, new ClockValue(second, second >= maxSecond ? ClockValue.Type.FINISHED : second >= (maxSecond - 10) ? ClockValue.Type.FLASH : ClockValue.Type.NORMAL));
	}

	@Override
	default void removeZone(ResourceLocation zone, int index) {
		var container = ZoneContainer.REGISTRY.get(zone);

		if (container != null) {
			container.remove(index);
		}

		for (var dim : ZoneLoader.SERVER_BY_DIMENSION.values()) {
			dim.remove(zone, index);
		}

		s2c(new RemoveZonePayload(zone, index));
	}

	@Override
	default void updateZone(ResourceLocation zone, int index, Zone zoneData) {
		var container = ZoneContainer.REGISTRY.get(zone);

		if (container != null) {
			container.update(index, zoneData);
		}

		for (var dim : ZoneLoader.SERVER_BY_DIMENSION.values()) {
			dim.update(zone, index, zoneData);
		}

		s2c(new UpdateZonePayload(zone, index, zoneData));
	}

	@Override
	default void setAnchor(Anchor anchor) {
		VLMinecraftEnvironment.super.setAnchor(anchor);

		for (var level : vl$self().getAllLevels()) {
			level.vl$updateLoadedChunks();
		}
	}

	default Map<UUID, GameProfile> vl$getReroutedPlayers() {
		var map = new Object2ObjectOpenHashMap<UUID, GameProfile>();
		var path = VidLibPaths.GAME.resolve("rerouted-players.json");

		if (Files.exists(path)) {
			try (var reader = Files.newBufferedReader(path)) {
				for (var entry : JsonUtils.read(reader).getAsJsonObject().entrySet()) {
					try {
						var from = retrieveGameProfile(entry.getKey());
						var to = retrieveGameProfile(UndashedUuid.fromString(entry.getValue().getAsString()));

						if (from != null && to != null) {
							map.put(from.getId(), to);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return map;
	}

	@Override
	default GameProfile retrieveGameProfile(UUID uuid) {
		try {
			var profile = vl$self().getSessionService().fetchProfile(uuid, true).profile();
			return profile == null ? Empty.PROFILE : profile;
		} catch (Exception ex) {
			return Empty.PROFILE;
		}
	}

	@Override
	default GameProfile retrieveGameProfile(String name) {
		try {
			return vl$self().getProfileCache().getAsync(name).get(5L, TimeUnit.SECONDS).orElse(Empty.PROFILE);
		} catch (Exception ex) {
			return Empty.PROFILE;
		}
	}

	@Override
	default void syncGlobalVariables() {
		s2c(new SyncGlobalNumberVariablesPayload(globalVariables()));
	}

	@Override
	default List<ServerSessionData> vl$getAllSessionData() {
		var players = vl$self().getPlayerList().getPlayers();
		var list = new ArrayList<ServerSessionData>(players.size());

		for (var player : players) {
			list.add(player.vl$sessionData());
		}

		return list;
	}

	@Override
	default String getServerBrand() {
		return "neoforge";
	}

	@Override
	default boolean isServerNeoForge() {
		return true;
	}

	default void runServerCommand(String command) {
		vl$self().getCommands().performPrefixedCommand(vl$self().createCommandSourceStack(), command);
	}

	@Nullable
	default PacketCapture vl$getPacketCapture(boolean start) {
		throw new NoMixinException(this);
	}

	default void vl$save() {
		getServerData().save(vl$self(), vl$self().getWorldPath(LevelResource.ROOT).resolve("vidlib.nbt"));

		var packetCapture = vl$getPacketCapture(false);

		if (packetCapture != null) {
			packetCapture.saveAll();
		}
	}
}
