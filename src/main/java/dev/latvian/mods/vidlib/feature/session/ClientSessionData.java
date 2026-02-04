package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.UUID;

public class ClientSessionData extends SessionData {
	private Set<String> currentTags;

	public ClientSessionData(UUID uuid) {
		super(uuid);
		this.currentTags = Set.of();
	}

	@Override
	public void updateOverrides(Player player) {
		super.updateOverrides(player);
		currentTags = dataMap.get(InternalPlayerData.PLAYER_TAGS, player.level().getGameTime());
	}

	@Override
	public Set<String> getTags() {
		return currentTags;
	}
}
