package dev.latvian.mods.vidlib.feature.platform;

import de.maxhenkel.voicechat.Voicechat;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.integration.voicechat.VoiceChatIntegration;
import dev.mrbeastgaming.mods.hub.api.data.HubUsedPort;
import dev.mrbeastgaming.mods.hub.api.gateway.AvailableWorldsEvent;
import dev.mrbeastgaming.mods.hub.api.gateway.HubWorldDirectory;
import dev.mrbeastgaming.mods.hub.api.gateway.UsedPortsEvent;
import dev.mrbeastgaming.mods.hub.event.SyncServerFilesHubEvent;
import dev.mrbeastgaming.mods.hub.file.HubFileUploads;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public class NeoForgeCommonGameEngine extends CommonGameEngine {
	@Override
	public void getUsedPorts(MinecraftServer server, List<HubUsedPort> list) {
		super.getUsedPorts(server, list);
		NeoForge.EVENT_BUS.post(new UsedPortsEvent(server, list));

		if (PlatformHelper.INSTANCE.isModLoaded(Voicechat.MODID)) {
			voiceChatIntegration(list);
		}
	}

	@Override
	public void getAvailableWorlds(MinecraftServer server, List<HubWorldDirectory> list) {
		super.getAvailableWorlds(server, list);
		NeoForge.EVENT_BUS.post(new AvailableWorldsEvent(server, list));
	}

	private void voiceChatIntegration(List<HubUsedPort> list) {
		VoiceChatIntegration.addUsedPorts(list);
	}

	@Override
	public void collectServerUploads(MinecraftServer server, HubFileUploads uploads) {
		super.collectServerUploads(server, uploads);
		NeoForge.EVENT_BUS.post(new SyncServerFilesHubEvent(uploads, server));
	}
}
