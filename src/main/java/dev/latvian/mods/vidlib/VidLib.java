package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.registry.CustomRegistryCollector;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.replay.api.ReplayMarkerGroup;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clock.ClockFont;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.font.TTFFile;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.misc.ReplayMarkerPayload;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VidLib {
	public static final String ID = "vidlib";
	public static final String NAME = "VidLib";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
	public static String VERSION = "dev";

	public static void init() {
		VidLib.LOGGER.info("VidLib " + VERSION + " loaded");

		if (PlatformHelper.CURRENT.getSide().isClient()) {
			initClient();
		}
	}

	private static void initClient() {
		VidLibClient.init();
	}

	public static void builtInRegistries(CustomRegistryCollector registry) {
		var platform = VLPlatformHelper.CURRENT;
		registry.register(ZoneShape.REGISTRY, platform::collectZoneShapes);
		registry.register(Icon.REGISTRY, platform::collectIcons);
		registry.register(ScreenShakeType.REGISTRY, platform::collectScreenShakeTypes);
		registry.register(BulkLevelModification.REGISTRY, platform::collectBulkLevelModifications);
		registry.register(ScreenEffect.REGISTRY, platform::collectScreenEffects);
		registry.register(Atmosphere.REGISTRY, null);
		registry.register(ZoneContainer.REGISTRY, null);
		registry.register(Cutscene.REGISTRY, null);
		registry.register(Location.REGISTRY, null);
		registry.register(ClockFont.REGISTRY, null);
		registry.register(Clock.REGISTRY, null);
		registry.register(TTFFile.REGISTRY, platform::collectTTFFiles);
	}

	public static void sync(ServerPlayer player, int syncType) {
		if (player.level().isReplayLevel()) {
			return;
		}

		var packets = new S2CPacketBundleBuilder(player.level());
		packets.s2c(new ReplayMarkerPayload(ReplayMarkerData.builder().group(ReplayMarkerGroup.DATA_SYNC).build()));
		player.vl$sessionData().sync(packets, player, syncType);
		packets.send(player);
	}

	public static void errorToHub(String message, Throwable ex) {
		LOGGER.error(message, ex);
		HubAPI.log(message, ex);
	}
}
