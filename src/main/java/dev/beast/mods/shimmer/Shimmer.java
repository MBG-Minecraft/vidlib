package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.misc.MarkerData;
import dev.beast.mods.shimmer.feature.misc.MarkerPayload;
import dev.beast.mods.shimmer.feature.net.S2CPacketBundleBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Mod(Shimmer.ID)
public class Shimmer {
	public static final String ID = "shimmer";
	public static final String NAME = "Shimmer";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public Shimmer() throws IOException {
		Shimmer.LOGGER.info("Shimmer loaded");

		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof DeferredRegister<?> reg) {
				var container = ModList.get().getModContainerById(s.mod().getModId());

				if (container.isPresent()) {
					var bus1 = container.get().getEventBus();

					if (bus1 != null) {
						reg.register(bus1);
					} else {
						Shimmer.LOGGER.error("Failed to find @AutoRegister mod event bus for " + s.mod().getModId());
					}
				} else {
					Shimmer.LOGGER.error("Failed to find @AutoRegister mod container for " + s.mod().getModId());
				}
			}
		}
	}

	public static void sync(ServerPlayer player, boolean login) {
		if (player.level().isReplayLevel()) {
			return;
		}

		var packets = new S2CPacketBundleBuilder(player.level());
		packets.s2c(new MarkerPayload(new MarkerData("sync", player)).toS2C(player.level()));
		player.shimmer$sessionData().sync(packets, player, login);
		packets.send(player);
	}
}
