package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumber;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.misc.EventMarkerPayload;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
		buildRegistries();
	}

	public static void buildRegistries() {
		KNumber.REGISTRY.build();
		KVector.REGISTRY.build();
		EntityFilter.REGISTRY.build();
		BlockFilter.REGISTRY.build();
		ZoneShape.REGISTRY.build();
		Icon.REGISTRY.build();
		ScreenShakeType.REGISTRY.build();
		BulkLevelModification.REGISTRY.build();
		ScreenEffect.REGISTRY.build();
		EntityNumber.REGISTRY.build();
	}

	public static void sync(ServerPlayer player, int syncType) {
		if (player.level().isReplayLevel()) {
			return;
		}

		var packets = new S2CPacketBundleBuilder(player.level());
		packets.s2c(new EventMarkerPayload("sync", Optional.empty()));
		player.vl$sessionData().sync(packets, player, syncType);
		packets.send(player);
	}
}
