package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.HubClientSessionData;
import dev.mrbeastgaming.mods.hub.api.HubGameServerData;
import dev.mrbeastgaming.mods.hub.api.HubKeyData;
import dev.mrbeastgaming.mods.hub.api.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.HubUserData;
import dev.mrbeastgaming.mods.hub.api.project.HubParticipantData;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectData;
import dev.mrbeastgaming.mods.hub.file.HubDirectoryUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubFileUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubUploadBuilderBase;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VidLibClient {
	public static void init() {
		loadHub();
	}

	public static void loadHub() {
		var userConfig = HubUserConfig.load();
		var data = HubClientSessionData.load(userConfig, HubProjectConfig.INSTANCE.get());
		HubUserData.SELF = data == null ? null : data.user();
		HubProjectData.PACK = data == null ? null : data.project().orElse(null);
		HubParticipantData.SELF = data == null ? null : data.participant().orElse(null);
		HubUserCapabilities.CURRENT = data == null ? HubUserCapabilities.DEFAULT : data.capabilities();
		HubGameServerData.CURRENT = data == null ? List.of() : data.servers();
		HubKeyData.KEYS = data == null ? null : data.keys();
		HubKeyData.SESSION_KEYS = data == null ? null : data.sessionKeys();
	}

	private static void wrapHubUploadBuilder(HubUploadBuilderBase builder) {
		var user = HubUserData.SELF;
		var userId = Minecraft.getInstance().getUser().getProfileId();

		if (user != null) {
			builder.setAssignedTo(user.id());
		}

		builder.setAssignedToMinecraft(userId);
	}

	public static ProgressQueue createUploadQueue() {
		var queue = new ProgressQueue("Uploading Files...");
		queue.bottomText = "Please keep the game open!";
		queue.hideInGame = true;
		return queue;
	}

	public static Consumer<HubDirectoryUploadBuilder> wrapHubDirectoryUploadBuilder(Consumer<HubDirectoryUploadBuilder> parent) {
		return builder -> {
			wrapHubUploadBuilder(builder);
			parent.accept(builder);
		};
	}

	public static BiConsumer<FileInfo, HubFileUploadBuilder> wrapHubFileUploadBuilder(BiConsumer<FileInfo, HubFileUploadBuilder> parent) {
		return (file, builder) -> {
			wrapHubUploadBuilder(builder);
			parent.accept(file, builder);
		};
	}
}
