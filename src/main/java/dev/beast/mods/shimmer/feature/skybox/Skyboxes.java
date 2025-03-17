package dev.beast.mods.shimmer.feature.skybox;

import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.resources.ResourceLocation;

public interface Skyboxes {
	ResourceLocation DEFAULT = Shimmer.id("default");

	ResourceLocation DAY = Shimmer.id("day");
	ResourceLocation NIGHT = Shimmer.id("night");
	ResourceLocation STORM = Shimmer.id("storm");
	ResourceLocation DOOM = Shimmer.id("doom");

	ResourceLocation PURPLE_SPACE = Shimmer.id("purple_space");
}
