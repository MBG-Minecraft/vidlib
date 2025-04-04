package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.clock.ClockValue;
import dev.beast.mods.shimmer.feature.clock.SyncClocksPayload;
import dev.beast.mods.shimmer.feature.data.SyncServerDataPayload;
import dev.beast.mods.shimmer.feature.misc.MarkerData;
import dev.beast.mods.shimmer.feature.net.S2CPacketBundleBuilder;
import dev.beast.mods.shimmer.feature.zone.Anchor;
import dev.beast.mods.shimmer.feature.zone.RemoveZonePayload;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneLoader;
import dev.beast.mods.shimmer.util.PauseType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public interface ShimmerMinecraftServer extends ShimmerMinecraftEnvironment {
	default MinecraftServer shimmer$self() {
		return (MinecraftServer) this;
	}

	@Override
	default boolean shimmer$isClient() {
		return false;
	}

	@Override
	default ServerLevel shimmer$level() {
		return shimmer$self().overworld();
	}

	@Override
	default PauseType getPauseType() {
		var server = shimmer$self();
		return server.isPaused() ? PauseType.GAME : server.tickRateManager().isFrozen() ? PauseType.TICK : PauseType.NONE;
	}

	@Override
	default List<? extends Player> shimmer$getS2CPlayers() {
		return shimmer$self().getPlayerList().getPlayers();
	}

	@ApiStatus.Internal
	default void sync(S2CPacketBundleBuilder packets, ServerPlayer player) {
		getServerData().syncAll(packets, null, (uuid, updates) -> new SyncServerDataPayload(updates));
		packets.s2c(new SyncClocksPayload(shimmer$getClocks()));
	}

	@ApiStatus.Internal
	default void shimmer$playerJoined(ServerPlayer player) {
		Shimmer.sync(player, true);
		player.server.marker(new MarkerData("player/logged_in", player));
	}

	@Override
	@ApiStatus.Internal
	default void shimmer$preTick(PauseType paused) {
		for (var level : shimmer$self().getAllLevels()) {
			var zones = ZoneLoader.BY_DIMENSION.get(level.dimension());
			level.shimmer$setActiveZones(zones);

			if (zones != null) {
				zones.tick(level);
			}
		}

		for (var player : shimmer$self().getPlayerList().getPlayers()) {
			player.shimmer$sessionData().shimmer$preTick(player);
		}
	}

	@Override
	@ApiStatus.Internal
	default void shimmer$postTick(PauseType paused) {
		var packetsToEveryone = new S2CPacketBundleBuilder(shimmer$level());

		getServerData().sync(packetsToEveryone, null, (playerId, update) -> new SyncServerDataPayload(update));

		for (var player : shimmer$self().getPlayerList().getPlayers()) {
			player.shimmer$sessionData().shimmer$postTick(packetsToEveryone, player);
		}

		packetsToEveryone.send(this);
	}

	default void betterDefaultGameRules() {
		var server = shimmer$self();
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

	default Map<ResourceLocation, ClockValue> shimmer$getClocks() {
		throw new NoMixinException(this);
	}

	default void setClock(ResourceLocation id, ClockValue value) {
		var map = shimmer$getClocks();

		if (!Objects.equals(map.put(id, value), value)) {
			s2c(new SyncClocksPayload(map));
		}
	}

	default void resetClock(ResourceLocation id) {
		var map = shimmer$getClocks();

		if (map.remove(id) != null) {
			s2c(new SyncClocksPayload(map));
		}
	}

	default void resetAllClocks() {
		var map = shimmer$getClocks();

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
		ShimmerMinecraftEnvironment.super.setAnchor(anchor);

		for (var level : shimmer$self().getAllLevels()) {
			level.shimmer$updateLoadedChunks();
		}
	}
}
