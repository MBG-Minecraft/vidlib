package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.util.CustomPacketHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;

public interface ShimmerMinecraftServer extends ShimmerMinecraftEnvironment, CustomPacketHandler {
	ZoneContainer shimmer$getZoneContainer();

	void refreshZones();

	@ApiStatus.Internal
	void shimmer$playerJoined(ServerPlayer player);

	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToAllPlayers(packet);
	}

	default void defaultGameRules() {
		var server = (MinecraftServer) this;
		server.overworld().setDayTime(6000L);
		server.overworld().setWeatherParameters(20000000, 20000000, false, false);

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
