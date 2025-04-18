package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.misc.MarkerData;
import dev.beast.mods.shimmer.feature.misc.MarkerPayload;
import dev.beast.mods.shimmer.feature.net.S2CPacketBundleBuilder;
import dev.beast.mods.shimmer.util.Lazy;
import dev.beast.mods.shimmer.util.MiscUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(Shimmer.ID)
public class Shimmer {
	public static final String ID = "shimmer";
	public static final String NAME = "Shimmer";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public static final Path PATH = FMLPaths.GAMEDIR.get().resolve("shimmer");
	public static final Lazy<Path> HOME_DIR = Lazy.of(() -> MiscUtils.createDir(Path.of(System.getenv().getOrDefault("SHIMMER_HOME", System.getProperty("user.home") + "/.shimmer"))));

	public static final ResourceKey<Level> LOBBY_DIMENSION = ResourceKey.create(Registries.DIMENSION, id("lobby"));

	public Shimmer() throws IOException {
		Shimmer.LOGGER.info("Shimmer loaded");

		if (Files.notExists(PATH)) {
			Files.createDirectories(PATH);
		}

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
