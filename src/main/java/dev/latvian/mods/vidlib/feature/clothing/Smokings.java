package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.ResourceKey;

public interface Smokings {
	static ResourceKey<ClothingSet> create(String id) {
		return ClothingPresets.createId(VidLib.id("smoking/" + id));
	}

	ResourceKey<ClothingSet> SHINY_BLACK = create("shiny_black");
	ResourceKey<ClothingSet> SHINY_BLUE = create("shiny_blue");
	ResourceKey<ClothingSet> SHINY_GREEN = create("shiny_green");
	ResourceKey<ClothingSet> SHINY_YELLOW = create("shiny_yellow");
	ResourceKey<ClothingSet> SHINY_RED = create("shiny_red");
	ResourceKey<ClothingSet> SHINY_PURPLE = create("shiny_purple");
	ResourceKey<ClothingSet> SHINY_WHITE = create("shiny_white");
}