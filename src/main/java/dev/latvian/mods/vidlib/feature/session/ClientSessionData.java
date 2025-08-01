package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.data.DataMapOverrides;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClientSessionData extends SessionData {
	public final Set<String> tags;

	public ClientSessionData(UUID uuid) {
		super(uuid);
		this.tags = new HashSet<>(0);
	}

	@Override
	public Set<String> getTags(long gameTime) {
		if (dataMap.overrides != null) {
			var v = dataMap.overrides.getOverride(DataMapOverrides.PLAYER_TAGS, gameTime);

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
