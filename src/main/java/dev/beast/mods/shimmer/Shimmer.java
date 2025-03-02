package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.content.clock.ClockContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
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

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

	public static final ResourceKey<Level> LOBBY_DIMENSION = ResourceKey.create(Registries.DIMENSION, id("lobby"));

	public static boolean defaultGameRules = true;
	public static boolean loadVanillaStructures = false;

	public Shimmer(IEventBus bus, Dist dist) throws IOException {
		Shimmer.LOGGER.info("Shimmer loaded");

		if (Files.notExists(PATH)) {
			Files.createDirectories(PATH);
		}

		ITEMS.register(bus);
		BLOCKS.register(bus);
		BLOCK_ENTITIES.register(bus);

		ClockContent.init();
	}
}
