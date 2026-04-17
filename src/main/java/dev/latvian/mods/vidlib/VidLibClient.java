package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.HubClientSessionData;
import dev.mrbeastgaming.mods.hub.api.HubUserData;
import dev.mrbeastgaming.mods.hub.file.HubFileUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.UniqueIdProvider;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class VidLibClient {
	public static void init() {
		HubClientSessionData.load();
	}

	public static Consumer<HubFileUploadBuilder> wrapHubFileUploadBuilder(Consumer<HubFileUploadBuilder> parent) {
		return builder -> {
			var user = HubUserData.SELF;
			var userId = Minecraft.getInstance().getUser().getProfileId();

			builder.setUniqueId(UniqueIdProvider.ofUUIDAndFileName(userId));

			if (user != null) {
				builder.setAssignedTo(user.id());
			}

			builder.setAssignedToMinecraft(userId);

			var queue = new ProgressQueue("Uploading Files...");
			queue.bottomText = "Please keep the game open!";
			queue.hideInGame = true;
			builder.setProgressQueue(queue);

			parent.accept(builder);
		};
	}
}
