package dev.latvian.mods.vidlib.feature.integration.voicechat;

import de.maxhenkel.voicechat.Voicechat;
import dev.mrbeastgaming.mods.hub.api.data.HubUsedPort;

import java.util.List;

public class VoiceChatIntegration {
	public static void addUsedPorts(List<HubUsedPort> list) {
		list.add(HubUsedPort.udp("Voice Chat", Voicechat.SERVER_CONFIG.voiceChatPort.get()));
	}
}
