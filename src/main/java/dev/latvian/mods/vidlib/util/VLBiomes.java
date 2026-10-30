package dev.latvian.mods.vidlib.util;

import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

public interface VLBiomes {
	Biome VOID = new Biome.BiomeBuilder()
		.hasPrecipitation(false)
		.temperature(0.5F)
		.downfall(0.5F)
		.setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(12638463))
		.setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(7907327))
		.setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(329011))
		.setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.EMPTY)
		.setAttribute(EnvironmentAttributes.CREATURE_WORLD_GEN_SPAWN_PROBABILITY, 0F)
		.mobSpawnSettings(new MobSpawnSettings.Builder().build())
		.specialEffects(new BiomeSpecialEffects.Builder()
			.waterColor(4159204)
			.build()
		)
		.generationSettings(new BiomeGenerationSettings.Builder(null, null).build())
		.build();
}
