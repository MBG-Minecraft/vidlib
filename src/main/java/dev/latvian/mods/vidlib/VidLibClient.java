package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.HubClientSessionData;
import dev.mrbeastgaming.mods.hub.api.HubUserData;
import dev.mrbeastgaming.mods.hub.file.HubDirectoryUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubFileUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubUploadBuilderBase;
import net.minecraft.client.Minecraft;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VidLibClient {
	public static void init() {
		HubClientSessionData.load();
	}

	private static void wrapHubUploadBuilder(HubUploadBuilderBase builder) {
		var user = HubUserData.SELF;
		var userId = Minecraft.getInstance().getUser().getProfileId();

		if (user != null) {
			builder.setAssignedTo(user.id());
		}

		builder.setAssignedToMinecraft(userId);

		var queue = new ProgressQueue("Uploading Files...");
		queue.bottomText = "Please keep the game open!";
		queue.hideInGame = true;
		builder.setProgressQueue(queue);
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
