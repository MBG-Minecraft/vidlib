package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.misc.MarkerData;
import dev.latvian.mods.vidlib.feature.misc.MarkerPayload;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Mod(VidLib.ID)
public class VidLib {
	public static final String ID = "vidlib";
	public static final String NAME = "VidLib";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public VidLib(IEventBus bus) throws IOException {
		LOGGER.info("VidLib loaded");
		VidLibDataTypes.register();

		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof DeferredRegister<?> reg) {
				var container = ModList.get().getModContainerById(s.mod().getModId());

				if (container.isPresent()) {
					var bus1 = container.get().getEventBus();

					if (bus1 != null) {
						reg.register(bus1);
					} else {
						LOGGER.error("Failed to find @AutoRegister mod event bus for " + s.mod().getModId());
					}
				} else {
					LOGGER.error("Failed to find @AutoRegister mod container for " + s.mod().getModId());
				}
			}
		}

		VidLibContent.init(bus);
	}

	public static void setupSync() {
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
