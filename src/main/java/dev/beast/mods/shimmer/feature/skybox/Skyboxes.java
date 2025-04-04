package dev.beast.mods.shimmer.feature.skybox;

import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.resources.ResourceLocation;

public interface Skyboxes {
	ResourceLocation DEFAULT = Shimmer.id("default");

	ResourceLocation DAY = Shimmer.id("day");
	ResourceLocation DAY_WITH_CELESTIALS = Shimmer.id("day_with_celestials");
	ResourceLocation NIGHT = Shimmer.id("night");
	ResourceLocation NIGHT_WITH_CELESTIALS = Shimmer.id("night_with_celestials");
	ResourceLocation STORM = Shimmer.id("storm");
	ResourceLocation DOOM = Shimmer.id("doom");
	ResourceLocation BRIGHT_NIGHT = Shimmer.id("bright_night");
	ResourceLocation BLACK_VOID = Shimmer.id("black_void");
	ResourceLocation WHITE_VOID = Shimmer.id("white_void");
}
