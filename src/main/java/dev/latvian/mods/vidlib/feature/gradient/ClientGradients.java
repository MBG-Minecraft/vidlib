package dev.latvian.mods.vidlib.feature.gradient;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.Ref;

public interface ClientGradients {
	static Ref<Gradient> ref(String key) {
		return Gradient.REGISTRY.ref(key);
	}

	Ref<Gradient> TRAIL = ref("trail");
	Ref<Gradient> FIRE_1 = ref("fire/1");
	Ref<Gradient> FIRE_2 = ref("fire/2");
	Ref<Gradient> FIRE_3 = ref("fire/3");
	Ref<Gradient> FIRE_4 = ref("fire/4");
	Ref<Gradient> SPARK = ref("spark");

	Ref<Gradient> BLACK_BASE = ref("black_base");
	Ref<Gradient> WHITE_BASE = ref("white_base");

	Ref<Gradient> BLACK = ref("black");
	Ref<Gradient> BROWN = ref("brown");
	Ref<Gradient> BLUE = ref("blue");
	Ref<Gradient> CYAN = ref("cyan");
	Ref<Gradient> SQUID = ref("squid");
	Ref<Gradient> OLIVE = ref("olive");
	Ref<Gradient> GREEN = ref("green");
	Ref<Gradient> LIME = ref("lime");
	Ref<Gradient> YELLOW = ref("yellow");
	Ref<Gradient> GRAPEFRUIT = ref("grapefruit");
	Ref<Gradient> ORANGE = ref("orange");
	Ref<Gradient> RED = ref("red");
	Ref<Gradient> MAGENTA = ref("magenta");
	Ref<Gradient> PURPLE = ref("purple");
	Ref<Gradient> PINK = ref("pink");
	Ref<Gradient> WHITE = ref("white");
	Ref<Gradient> GRAY = ref("gray");

	Ref<Gradient> DARK_BLUE = ref("dark_blue");
	Ref<Gradient> DARK_GREEN = ref("dark_green");
	Ref<Gradient> DARK_YELLOW = ref("dark_yellow");
	Ref<Gradient> DARK_RED = ref("dark_red");

	Ref<Gradient> SHINY_BLUE = ref("shiny_blue");
	Ref<Gradient> SHINY_GREEN = ref("shiny_green");
	Ref<Gradient> SHINY_YELLOW = ref("shiny_yellow");
	Ref<Gradient> SHINY_RED = ref("shiny_red");
	Ref<Gradient> SHINY_PURPLE = ref("shiny_purple");
	Ref<Gradient> SHINY_WHITE = ref("shiny_white");
}
