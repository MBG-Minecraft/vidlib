package dev.latvian.mods.vidlib.feature.integration.voicechat;

import de.maxhenkel.voicechat.Voicechat;
import dev.mrbeastgaming.mods.hub.api.UsedPort;

import java.util.List;

public class VoiceChatIntegration {
	public static void addUsedPorts(List<UsedPort> list) {
		list.add(UsedPort.udp("Voice Chat", Voicechat.SERVER_CONFIG.voiceChatPort.get()));
	}
}
