package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.worldsync.ProgressingText;
import dev.beast.mods.shimmer.feature.worldsync.WorldSyncReadThread;
import dev.beast.mods.shimmer.feature.worldsync.WorldSyncScreen;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

import javax.annotation.Nullable;

public interface ShimmerChatComponent {
	default boolean shimmer$consumeMessage(Minecraft mc, Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
		var messageString = message.getString().toLowerCase().trim();

		if (messageString.startsWith("[world sync] started server @ ")) {
			String address = messageString.substring(30).trim();
			int index = address.lastIndexOf(':');
			String ip = address.substring(0, index);
			int port = Integer.parseInt(address.substring(index + 1));

			if (ip.equals("${ip}")) {
				var info = mc.getCurrentServer();
				ip = info == null ? "localhost" : info.ip;
				int ipp = ip.indexOf(':');

				if (ipp != -1) {
					ip = ip.substring(0, ipp);
				}
			}

			var screen = new WorldSyncScreen();
			screen.text.add(new ProgressingText().setText("Indexing files..."));
			screen.thread = new WorldSyncReadThread(mc, screen, ip, port);
			mc.setScreen(screen);
			return true;
		} else if (messageString.equals("[world sync] ready to connect!")) {
			if (mc.screen instanceof WorldSyncScreen screen) {
				screen.startThread();
			}

			return true;
		}

		return false;
	}
}
