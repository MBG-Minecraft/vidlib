package dev.latvian.mods.vidlib.integration;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.CreateGroupEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.GroupEvent;
import de.maxhenkel.voicechat.api.events.JoinGroupEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

@ForgeVoicechatPlugin
public class VidLibVoidChatPlugin implements VoicechatPlugin {
	private static UUID broadcastId = UUID.randomUUID();

	@Override
	public String getPluginId() {
		return "vidlib";
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(ClientVoicechatConnectionEvent.class, this::onClientVoicechatConnection);
		registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
		registration.registerEvent(CreateGroupEvent.class, this::onGroupEvent);
		registration.registerEvent(JoinGroupEvent.class, this::onGroupEvent);
	}

	private void onClientVoicechatConnection(ClientVoicechatConnectionEvent event) {
		var client = ClientManager.getClient();

		if (client != null && ClientGameEngine.INSTANCE.getRecordVoiceChat()) {
			client.setRecording(event.isConnected());
			VidLib.LOGGER.info("Set VoiceChat recording state to " + event.isConnected());
		}
	}

	// CreateGroupEvent and JoinGroupEvent
	private <E extends GroupEvent> void onGroupEvent(E event) {
		if (event.getConnection() == null) {
			return;
		}

		Object object = event.getConnection().getPlayer();
		if (!(object instanceof Player player) || player.level().isClientSide()) {
			return;
		}

		Group group = event.getGroup();
		if (group == null || !group.getName().strip().equalsIgnoreCase("broadcast")) {
			return;
		}

		if (!CommonGameEngine.INSTANCE.canVoicechatBroadcast(player)) {
			return;
		}

		event.cancel();
	}

	private void onMicrophone(MicrophonePacketEvent event) {
		if (event.getSenderConnection() == null) {
			return;
		}

		Object object = event.getSenderConnection().getPlayer().getPlayer();
		if (!(object instanceof Player player)) {
			return;
		}

		if (player.level().isClientSide() || !CommonGameEngine.INSTANCE.canVoicechatBroadcast(player)) {
			return;
		}

		Group group = event.getSenderConnection().getGroup();
		if (group == null || !group.getName().strip().equalsIgnoreCase("broadcast")) {
			return;
		}

		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}

		event.cancel();
		VoicechatServerApi api = event.getVoicechat();
		StaticSoundPacket builder = event.getPacket().staticSoundPacketBuilder()
			.channelId(broadcastId)
			.category("plugin")
			.build();

		for (Player sending : server.getPlayerList().getPlayers()) {
			VoicechatConnection connection = api.getConnectionOf(sending.getUUID());
			if (connection == null || sending.getUUID().equals(player.getUUID())) {
				continue;
			}
			api.sendStaticSoundPacketTo(connection, builder);
		}
	}

}
