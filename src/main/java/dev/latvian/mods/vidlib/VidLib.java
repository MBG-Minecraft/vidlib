package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.misc.MarkerData;
import dev.latvian.mods.vidlib.feature.misc.MarkerPayload;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class VidLib {
	public static final String ID = "vidlib";
	public static final String NAME = "VidLib";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
	public static final Path DIR = FMLPaths.GAMEDIR.get().resolve("vidlib");
	public static final Path LOCAL_DIR = FMLPaths.GAMEDIR.get().resolve("local/vidlib");

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public static void init() {
		VidLib.LOGGER.info("VidLib loaded");
		VidLibDataTypes.register();
	}

	public static void sync(ServerPlayer player, int syncType) {
		if (player.level().isReplayLevel()) {
			return;
		}

		var packets = new S2CPacketBundleBuilder(player.level());
		packets.s2c(new MarkerPayload(new MarkerData("sync", player)).toS2C(player.level()));
		player.vl$sessionData().sync(packets, player, syncType);
		packets.send(player);
	}
}
