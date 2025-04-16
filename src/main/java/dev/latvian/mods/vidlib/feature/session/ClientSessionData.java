package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.util.Empty;
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
