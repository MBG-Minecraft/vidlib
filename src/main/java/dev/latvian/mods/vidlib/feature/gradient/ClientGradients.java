package dev.latvian.mods.vidlib.feature.gradient;

import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.color.GradientReference;
import dev.latvian.mods.vidlib.VidLib;

public interface ClientGradients {
	static Gradient ref(String path) {
		return new GradientReference(ID.vidlib(path));
	}

	Gradient TRAIL = ref("trail");
	Gradient FIRE_1 = ref("fire/1");
	Gradient FIRE_2 = ref("fire/2");
	Gradient FIRE_3 = ref("fire/3");
	Gradient FIRE_4 = ref("fire/4");
	Gradient SPARK = ref("spark");

	Gradient BLACK_BASE = ref("black_base");
	Gradient WHITE_BASE = ref("white_base");

	Gradient BLACK = ref("black");
	Gradient BROWN = ref("brown");
	Gradient BLUE = ref("blue");
	Gradient CYAN = ref("cyan");
	Gradient SQUID = ref("squid");
	Gradient OLIVE = ref("olive");
	Gradient GREEN = ref("green");
	Gradient LIME = ref("lime");
	Gradient YELLOW = ref("yellow");
	Gradient GRAPEFRUIT = ref("grapefruit");
	Gradient ORANGE = ref("orange");
	Gradient RED = ref("red");
	Gradient MAGENTA = ref("magenta");
	Gradient PURPLE = ref("purple");
	Gradient PINK = ref("pink");
	Gradient WHITE = ref("white");
	Gradient GRAY = ref("gray");

	Gradient DARK_BLUE = ref("dark_blue");
	Gradient DARK_GREEN = ref("dark_green");
	Gradient DARK_YELLOW = ref("dark_yellow");
	Gradient DARK_RED = ref("dark_red");

	Gradient SHINY_BLUE = ref("shiny_blue");
	Gradient SHINY_GREEN = ref("shiny_green");
	Gradient SHINY_YELLOW = ref("shiny_yellow");
	Gradient SHINY_RED = ref("shiny_red");
	Gradient SHINY_PURPLE = ref("shiny_purple");
	Gradient SHINY_WHITE = ref("shiny_white");
}
