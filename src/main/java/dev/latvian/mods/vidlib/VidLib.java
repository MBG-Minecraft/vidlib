package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.registry.CustomRegistryCollector;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.replay.api.ReplayMarkerGroup;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.ffmpeg.FFMPEGBinaries;
import dev.latvian.mods.vidlib.feature.misc.ReplayMarkerPayload;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.mrbeastgaming.mods.hub.HubConfig;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VidLib {
	public static final String ID = "vidlib";
	public static final String NAME = "VidLib";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
	public static String VERSION = "unknown";

	public static void init() {
		VidLib.LOGGER.info("VidLib " + VERSION + " loaded");
		Runtime.getRuntime().addShutdownHook(new Thread(HubServerGateway::stopGateway, "Stop-Server-Hub-Gateway"));

		if (PlatformHelper.CURRENT.isDevEnv()) {
			FFMPEGBinaries.initialize();
		}

		HubConfig.load();
	}

	public static void initClient() {
		VidLibClient.init();
	}

	public static void builtInRegistries(CustomRegistryCollector registry) {
		var platform = VLPlatformHelper.CURRENT;
		registry.register(ZoneShape.REGISTRY, platform::collectZoneShapeTypes);
		registry.register(ScreenShakeType.REGISTRY, platform::collectScreenShakeTypes);
		registry.register(ScreenEffect.REGISTRY, platform::collectScreenEffectTypes);
	}

	public static void sync(ServerPlayer player, int syncType) {
		if (VLPlatformHelper.CURRENT.isReplayLevel(player.level())) {
			return;
		}

		var packets = new S2CPacketBundleBuilder(player.level());
		packets.s2c(new ReplayMarkerPayload(ReplayMarkerData.builder().group(ReplayMarkerGroup.DATA_SYNC).build()));
		player.vl$sessionData().sync(packets, player, syncType);
		packets.send(player);
	}

	public static void errorToHub(String message, Throwable ex) {
		LOGGER.error(message, ex);

		var gateway = HubAPI.getClientOrServerGateway();

		if (gateway != null) {
			gateway.log(0, null, message, ex);
		}
	}
}
