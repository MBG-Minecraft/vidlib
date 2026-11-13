package dev.latvian.mods.vidlib.feature.gradient;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.Ref;

public interface VLGradients {
	static Ref<Gradient> ref(String key) {
		return Gradient.REGISTRY.ref(key);
	}

	Ref<Gradient> TRAIL = ref("trail");
	Ref<Gradient> FIRE_1 = ref("fire/1");
	Ref<Gradient> FIRE_2 = ref("fire/2");
	Ref<Gradient> FIRE_3 = ref("fire/3");
	Ref<Gradient> FIRE_4 = ref("fire/4");
	Ref<Gradient> SOUL_FIRE_1 = ref("soul_fire/1");
	Ref<Gradient> SPARK = ref("spark");

	interface Fabric {
		Ref<Gradient> BLACK = ref("fabric/black");
		Ref<Gradient> BROWN = ref("fabric/brown");
		Ref<Gradient> BLUE = ref("fabric/blue");
		Ref<Gradient> CYAN = ref("fabric/cyan");
		Ref<Gradient> SQUID = ref("fabric/squid");
		Ref<Gradient> OLIVE = ref("fabric/olive");
		Ref<Gradient> GREEN = ref("fabric/green");
		Ref<Gradient> LIME = ref("fabric/lime");
		Ref<Gradient> YELLOW = ref("fabric/yellow");
		Ref<Gradient> GRAPEFRUIT = ref("fabric/grapefruit");
		Ref<Gradient> ORANGE = ref("fabric/orange");
		Ref<Gradient> RED = ref("fabric/red");
		Ref<Gradient> MAGENTA = ref("fabric/magenta");
		Ref<Gradient> PURPLE = ref("fabric/purple");
		Ref<Gradient> PINK = ref("fabric/pink");
		Ref<Gradient> WHITE = ref("fabric/white");
		Ref<Gradient> GRAY = ref("fabric/gray");

		interface Dark {
			Ref<Gradient> BLUE = ref("fabric/dark/blue");
			Ref<Gradient> GREEN = ref("fabric/dark/green");
			Ref<Gradient> YELLOW = ref("fabric/dark/yellow");
			Ref<Gradient> RED = ref("fabric/dark/red");
		}

		interface Shiny {
			Ref<Gradient> BLUE = ref("fabric/shiny/blue");
			Ref<Gradient> GREEN = ref("fabric/shiny/green");
			Ref<Gradient> YELLOW = ref("fabric/shiny/yellow");
			Ref<Gradient> RED = ref("fabric/shiny/red");
			Ref<Gradient> PURPLE = ref("fabric/shiny/purple");
			Ref<Gradient> WHITE = ref("fabric/shiny/white");
		}
	}
}
