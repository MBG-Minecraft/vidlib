package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.data.SyncPlayerDataPayload;
import dev.beast.mods.shimmer.feature.data.SyncServerDataPayload;
import dev.beast.mods.shimmer.feature.input.SyncPlayerInputToClient;
import dev.beast.mods.shimmer.feature.misc.RefreshNamePayload;
import dev.beast.mods.shimmer.feature.zone.ZoneLoader;
import dev.beast.mods.shimmer.util.S2CPacketBundleBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface ShimmerMinecraftServer extends ShimmerMinecraftEnvironment {
	default MinecraftServer shimmer$self() {
		return (MinecraftServer) this;
	}

	@Override
	default List<? extends Player> shimmer$getPlayers() {
		return shimmer$self().getPlayerList().getPlayers();
	}

	@ApiStatus.Internal
	default void shimmer$playerJoined(ServerPlayer player) {
		player.shimmer$sessionData().dataMap.load(shimmer$self(), shimmer$self().getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("shimmer").resolve(player.getUUID() + ".nbt"));
		player.refreshDisplayName();
		player.refreshTabListName();
		player.shimmer$sessionData().updateOverrides(player);

		var toNewPlayer = new S2CPacketBundleBuilder();

		getServerData().syncAll(toNewPlayer, null, (uuid, updates) -> new SyncServerDataPayload(updates));
		toNewPlayer.s2c(new RefreshNamePayload(player.getUUID()));

		for (var p : shimmer$self().getPlayerList().getPlayers()) {
			if (!p.getUUID().equals(player.getUUID())) {
				var s = p.shimmer$sessionData();

				toNewPlayer.s2c(new SyncPlayerInputToClient(p.getUUID(), s.input));
				s.dataMap.syncAll(toNewPlayer, player, SyncPlayerDataPayload::new);
			}
		}

		toNewPlayer.send(player);
	}

	@Override
	@ApiStatus.Internal
	default void shimmer$preTick() {
		for (var level : shimmer$self().getAllLevels()) {
			var zones = ZoneLoader.BY_DIMENSION.get(level.dimension());
			level.shimmer$setActiveZones(zones);

			if (zones != null) {
				zones.entityZones.clear();

				for (var container : zones) {
					container.tick(zones, level);
				}
			}
		}

		for (var instance : ClockInstance.SERVER.getMap().values()) {
			instance.tick(shimmer$self().getLevel(instance.clock.dimension()));
		}

		for (var player : shimmer$self().getPlayerList().getPlayers()) {
			player.shimmer$sessionData().shimmer$preTick(player);
		}
	}

	@Override
	@ApiStatus.Internal
	default void shimmer$postTick() {
		var packetsToEveryone = new S2CPacketBundleBuilder();

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
}
