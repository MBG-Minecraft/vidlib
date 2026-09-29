package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.resources.ResourceKey;

import java.util.List;

public interface Tracksuits {
	static ResourceKey<ClothingSet> create(String id) {
		return ClothingPresets.createId(ID.vidlib("tracksuit/" + id));
	}

	ResourceKey<ClothingSet> BLACK = create("black");
	ResourceKey<ClothingSet> GRAY = create("gray");
	ResourceKey<ClothingSet> WHITE = create("white");
	ResourceKey<ClothingSet> RED = create("red");
	ResourceKey<ClothingSet> PINK = create("pink");
	ResourceKey<ClothingSet> MAGENTA = create("magenta");
	ResourceKey<ClothingSet> PURPLE = create("purple");
	ResourceKey<ClothingSet> BLUE = create("blue");
	ResourceKey<ClothingSet> CYAN = create("cyan");
	ResourceKey<ClothingSet> GREEN = create("green");
	ResourceKey<ClothingSet> LIME = create("lime");
	ResourceKey<ClothingSet> YELLOW = create("yellow");
	ResourceKey<ClothingSet> ORANGE = create("orange");

	List<ResourceKey<ClothingSet>> COLORED = List.of(
		RED,
		PINK,
		MAGENTA,
		PURPLE,
		BLUE,
		CYAN,
		GREEN,
		LIME,
		YELLOW,
		ORANGE
	);

	ResourceKey<ClothingSet> SQUID = create("squid");

	ResourceKey<ClothingSet> DARK_BLUE = create("dark_blue");
	ResourceKey<ClothingSet> DARK_GREEN = create("dark_green");
	ResourceKey<ClothingSet> DARK_YELLOW = create("dark_yellow");
	ResourceKey<ClothingSet> DARK_RED = create("dark_red");
}