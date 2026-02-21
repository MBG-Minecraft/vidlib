package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.core.VLGameTimeProvider;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ClientSessionData extends SessionData {
	public ClientSessionData(UUID uuid, VLGameTimeProvider timeProvider) {
		super(uuid, timeProvider);
	}

	@Override
	public void updateOverrides(Player player) {
		super.updateOverrides(player);
	}
}
