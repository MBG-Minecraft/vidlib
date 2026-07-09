package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.core.VLScoreboard;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ServerTeams {
	CustomRegistry<ByteBuf, PlayerTeam> REGISTRY = CustomRegistry.<ByteBuf, PlayerTeam>builder()
		.keys(ID.vidlib("server_team"), "minecraft")
		.build();

	static void update(MinecraftServer server, VLScoreboard scoreboard) {
		REGISTRY.updateValues(scoreboard.vl$getTeams().values().stream().collect(Collectors.toMap(
			t -> Identifier.withDefaultNamespace(t.getName().toLowerCase(Locale.ROOT)),
			Function.identity()
		)));

		server.getPlayerList().getPlayers().forEach(REGISTRY::syncValues);
	}
}
