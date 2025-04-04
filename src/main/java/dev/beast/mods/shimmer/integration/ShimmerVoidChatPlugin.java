package dev.beast.mods.shimmer.integration;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import dev.beast.mods.shimmer.Shimmer;

@ForgeVoicechatPlugin
public class ShimmerVoidChatPlugin implements VoicechatPlugin {
	@Override
	public String getPluginId() {
		return "shimmer";
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(ClientVoicechatConnectionEvent.class, event -> {
			var client = ClientManager.getClient();

			if (client != null) {
				client.setRecording(event.isConnected());
				Shimmer.LOGGER.info("Set VoiceChat recording state to " + event.isConnected());
			}
		});
	}
}
