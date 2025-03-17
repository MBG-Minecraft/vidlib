package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.fml.loading.FMLPaths;
import org.spongepowered.asm.mixin.Mixin;

import java.nio.file.Files;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(ServerPacksSource.class)
public abstract class ServerPacksSourceMixin extends BuiltInPackSource {
	public ServerPacksSourceMixin(PackType packType, VanillaPackResources vanillaPack, ResourceLocation packDir, DirectoryValidator validator) {
		super(packType, vanillaPack, packDir, validator);
	}

	@Override
	protected void populatePackList(BiConsumer<String, Function<String, Pack>> consumer) {
		super.populatePackList(consumer);

		var path = FMLPaths.GAMEDIR.get().resolve("datapacks");

		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		discoverPacksInPath(path, consumer);
	}
}
