package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.core.ShimmerEntity;
import dev.beast.mods.shimmer.feature.data.DataRecorder;
import dev.beast.mods.shimmer.util.Empty;
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

	public Set<String> getTags(ShimmerEntity entity) {
		if (dataMap.overrides != null) {
			var v = dataMap.overrides.getOverride(DataRecorder.PLAYER_TAGS, entity.shimmer$level().getGameTime());

			if (v != null) {
				return v;
			}
		}

		return tags;
	}

	public Component modifyPlayerName(Component original) {
		if (namePrefix != null || nameSuffix != null || !Empty.isEmpty(nickname)) {
			var name = Component.empty();

			if (namePrefix != null) {
				name.append(namePrefix);
			}

			if (Empty.isEmpty(nickname)) {
				name.append(original);
			} else {
				name.append(nickname);
			}

			if (nameSuffix != null) {
				name.append(nameSuffix);
			}

			return name;
		}

		return original;
	}
}
