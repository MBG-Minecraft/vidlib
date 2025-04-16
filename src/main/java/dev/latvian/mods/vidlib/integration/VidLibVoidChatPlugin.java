package dev.latvian.mods.vidlib.integration;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import dev.latvian.mods.vidlib.VidLib;

@ForgeVoicechatPlugin
public class VidLibVoidChatPlugin implements VoicechatPlugin {
	@Override
	public String getPluginId() {
		return "vidlib";
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(ClientVoicechatConnectionEvent.class, event -> {
			var client = ClientManager.getClient();

			if (client != null) {
				client.setRecording(event.isConnected());
				VidLib.LOGGER.info("Set VoiceChat recording state to " + event.isConnected());
			}
		});
	}
}
