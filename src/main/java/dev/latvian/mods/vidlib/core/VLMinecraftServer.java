package dev.latvian.mods.vidlib.core;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.clock.SyncClocksPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.misc.MarkerData;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.RemoveZonePayload;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneLoader;
import dev.latvian.mods.vidlib.util.Empty;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.PauseType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Files;
import java.util.HashMap;
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

	@Override
	default PauseType getPauseType() {
		var server = vl$self();
		return server.isPaused() ? PauseType.GAME : server.tickRateManager().isFrozen() ? PauseType.TICK : PauseType.NONE;
	}

	@Override
	default List<? extends Player> vl$getS2CPlayers() {
		return vl$self().getPlayerList().getPlayers();
	}

	@ApiStatus.Internal
	default void sync(S2CPacketBundleBuilder packets, ServerPlayer player) {
		getServerData().syncAll(packets, null, (uuid, updates) -> new SyncServerDataPayload(updates));
		packets.s2c(new SyncClocksPayload(vl$getClocks()));
	}

	@ApiStatus.Internal
	default void vl$playerJoined(ServerPlayer player) {
		VidLib.sync(player, true);
		player.server.marker(new MarkerData("player/logged_in", player));
	}

	@Override
	@ApiStatus.Internal
	default void vl$preTick(PauseType paused) {
		for (var level : vl$self().getAllLevels()) {
			level.vl$preTick(paused);

			var zones = ZoneLoader.BY_DIMENSION.get(level.dimension());
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

	default Map<ResourceLocation, ClockValue> vl$getClocks() {
		throw new NoMixinException(this);
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
	default void removeZone(UUID uuid) {
		for (var container : ZoneContainer.REGISTRY.getMap().values()) {
			container.remove(uuid);
		}

		for (var dim : ZoneLoader.BY_DIMENSION.values()) {
			dim.remove(uuid);
		}

		s2c(new RemoveZonePayload(uuid));
	}

	@Override
	default void setAnchor(Anchor anchor) {
		VLMinecraftEnvironment.super.setAnchor(anchor);

		for (var level : vl$self().getAllLevels()) {
			level.vl$updateLoadedChunks();
		}
	}

	default Map<UUID, GameProfile> vl$getReroutedPlayers() {
		var map = new HashMap<UUID, GameProfile>();
		var path = FMLPaths.GAMEDIR.get().resolve("vidlib/rerouted-players.json");

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
}
