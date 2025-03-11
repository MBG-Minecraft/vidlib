package dev.beast.mods.shimmer.feature.session;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShimmerRemoteClientSessionData extends ShimmerClientSessionData {
	public final Set<String> tags;

	public ShimmerRemoteClientSessionData(UUID uuid) {
		super(uuid);
		this.tags = new HashSet<>(0);
	}

	public void preTick(Minecraft mc, ClientLevel level, RemotePlayer player) {
		updateOverrides(player);
	}
}
