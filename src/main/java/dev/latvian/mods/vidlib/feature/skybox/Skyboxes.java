package dev.latvian.mods.vidlib.feature.skybox;

import dev.latvian.mods.vidlib.feature.registry.ID;
import net.minecraft.resources.ResourceLocation;

public interface Skyboxes {
	ResourceLocation VANILLA = ID.mc("vanilla");

	ResourceLocation DAY = ID.mc("day");
	ResourceLocation DAY_WITH_CELESTIALS = ID.mc("day_with_celestials");
	ResourceLocation NIGHT = ID.mc("night");
	ResourceLocation NIGHT_WITH_CELESTIALS = ID.mc("night_with_celestials");
	ResourceLocation STORM = ID.mc("storm");
	ResourceLocation DOOM = ID.mc("doom");
	ResourceLocation BRIGHT_NIGHT = ID.mc("bright_night");

	ResourceLocation BLACK_VOID = ID.mc("void/black");
	ResourceLocation WHITE_VOID = ID.mc("void/white");
	ResourceLocation GREEN_VOID = ID.mc("void/green");
	ResourceLocation BLUE_VOID = ID.mc("void/blue");
}
