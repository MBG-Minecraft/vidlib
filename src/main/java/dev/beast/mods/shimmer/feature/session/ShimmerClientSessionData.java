package dev.beast.mods.shimmer.feature.session;

import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShimmerClientSessionData extends ShimmerSessionData {
	public final Set<String> tags;

	public ShimmerClientSessionData(UUID uuid) {
		super(uuid);
		this.tags = new HashSet<>(0);
	}

	public Component modifyPlayerName(Component original) {
		if (namePrefix != null) {
			return Component.empty().append(namePrefix).append(original);
		}

		return original;
	}
}
