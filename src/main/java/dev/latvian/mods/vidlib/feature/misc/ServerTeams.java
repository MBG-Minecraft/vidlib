package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.registry.CustomRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ServerTeams {
	CustomRegistry<ByteBuf, PlayerTeam> REGISTRY = CustomRegistry.create("server_team");

	static void update(MinecraftServer server, ServerScoreboard scoreboard) {
		REGISTRY.updateValues(scoreboard.teamsByName.values().stream().collect(Collectors.toMap(
			t -> Identifier.withDefaultNamespace(t.getName().toLowerCase(Locale.ROOT)),
			Function.identity()
		)));

		server.getPlayerList().getPlayers().forEach(REGISTRY::syncValues);
	}
}
