package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.particle.ShimmerParticles;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
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

	public static ResourceLocation idFromString(String string) {
		return string.indexOf(':') == -1 ? Shimmer.id(string) : ResourceLocation.parse(string);
	}

	public static String idToString(ResourceLocation rl) {
		return rl.getNamespace().equals(Shimmer.ID) ? rl.getPath() : rl.toString();
	}

	public static final Path PATH = FMLPaths.GAMEDIR.get().resolve("shimmer");

	public static final ResourceKey<Level> LOBBY_DIMENSION = ResourceKey.create(Registries.DIMENSION, id("lobby"));

	public Shimmer(IEventBus bus, Dist dist) throws IOException {
		Shimmer.LOGGER.info("Shimmer loaded");

		if (Files.notExists(PATH)) {
			Files.createDirectories(PATH);
		}

		ShimmerArgumentTypes.REGISTRY.register(bus);
		ShimmerParticles.REGISTRY.register(bus);
	}
}
