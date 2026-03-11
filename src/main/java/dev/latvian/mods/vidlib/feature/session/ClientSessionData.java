package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.util.LevelGameTimeProvider;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ClientSessionData extends SessionData {
	public ClientSessionData(UUID uuid, LevelGameTimeProvider timeProvider) {
		super(uuid, timeProvider);
	}

	@Override
	public void updateOverrides(Player player) {
		super.updateOverrides(player);
	}
}
