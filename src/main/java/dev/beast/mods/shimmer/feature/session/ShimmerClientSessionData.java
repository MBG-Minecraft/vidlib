package dev.beast.mods.shimmer.feature.session;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ShimmerClientSessionData extends ShimmerSessionData {
	public ShimmerClientSessionData(UUID uuid) {
		super(uuid);
	}

	protected void updateSessionData(@Nullable Player player, List<PlayerData> update) {
		for (var data : update) {
			init(data.type()).playerData = data;

			if (player != null) {
				data.onDataReceived(player);
			}
		}
	}
}
