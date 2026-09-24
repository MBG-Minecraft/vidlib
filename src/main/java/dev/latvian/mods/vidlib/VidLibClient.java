package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.data.HubPossibleUser;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import dev.mrbeastgaming.mods.hub.event.SyncClientFilesHubEvent;
import dev.mrbeastgaming.mods.hub.file.HubDirectoryUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubFileUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubFileUploads;
import dev.mrbeastgaming.mods.hub.file.HubUploadBuilderBase;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VidLibClient {
	public static int levelTick = 0;

	public static void init() {
		MiscUtils.CLIENT_PLAYER.setValue(() -> Minecraft.getInstance().player);
		loadHub();
		Runtime.getRuntime().addShutdownHook(new Thread(HubClientGateway::stopGateway, "Stop-Client-Hub-Gateway"));
	}

	public static void loadHub() {
		var userConfig = HubUserConfig.load();
		HubAPI.CLIENT_GATEWAY.setValue(HubClientGateway::get);
		HubClientSession.load(Minecraft.getInstance(), userConfig, HubProjectConfig.INSTANCE.get());
	}

	private static void wrapHubUploadBuilder(HubUploadBuilderBase builder) {
		var user = HubClientSession.CURRENT.user;
		var userId = Minecraft.getInstance().getUser().getProfileId();

		if (user != null) {
			builder.setAssignedTo(HubPossibleUser.of(user));
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

	public static void checkFileSync(boolean isFirstTime) {
		if (HubUserCapabilities.get().autoUploadFiles()) {
			var entries = new ArrayList<HubFileUploads.Entry>();
			var event = new SyncClientFilesHubEvent(entries, isFirstTime);
			NeoForge.EVENT_BUS.post(event);

			if (!entries.isEmpty()) {
				HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> HubFileUploads.syncFiles(HubClientSession.CURRENT.uploadContext, entries, createUploadQueue()));
			}
		}
	}
}
