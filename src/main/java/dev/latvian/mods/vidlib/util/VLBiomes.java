package dev.latvian.mods.vidlib.util;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.world.level.biome.Biome;

import java.nio.file.Files;

public interface VLBiomes {
	Lazy<Biome> VOID = Lazy.of(() -> {
		try {
			var file = PlatformHelper.CURRENT.findVidLibFile("data", VidLib.ID, "worldgen", "biome", "void.json");
			try (var reader = Files.newBufferedReader(file)) {
				var json = JsonUtils.read(reader);
				return Biome.DIRECT_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	});
}
