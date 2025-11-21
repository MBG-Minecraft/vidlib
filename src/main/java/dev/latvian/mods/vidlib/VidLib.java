package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.misc.EventMarkerData;
import dev.latvian.mods.vidlib.feature.misc.EventMarkerPayload;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VidLib {
	public static final String ID = "vidlib";
	public static final String NAME = "VidLib";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
	public static String VERSION = "dev";

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public static void init() {
		VidLib.LOGGER.info("VidLib " + VERSION + " loaded");
		VidLibDataTypes.register();
	}

	public static void sync(ServerPlayer player, int syncType) {
		if (player.level().isReplayLevel()) {
			return;
		}

		var packets = new S2CPacketBundleBuilder(player.level());
		packets.s2c(new EventMarkerPayload(new EventMarkerData("sync", player)));
		player.vl$sessionData().sync(packets, player, syncType);
		packets.send(player);
	}
}
