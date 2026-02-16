package dev.latvian.mods.vidlib.util;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

public interface VLBiomes {
	Biome VOID = new Biome.BiomeBuilder()
		.hasPrecipitation(false)
		.temperature(0.5F)
		.downfall(0.5F)
		.specialEffects(new BiomeSpecialEffects.Builder()
			.fogColor(12638463)
			.skyColor(7907327)
			.waterColor(4159204)
			.waterFogColor(329011)
			.silenceAllBackgroundMusic()
			.build()
		)
		.mobSpawnSettings(new MobSpawnSettings.Builder().creatureGenerationProbability(0F).build())
		.generationSettings(new BiomeGenerationSettings.Builder(null, null).build())
		.build();
}
