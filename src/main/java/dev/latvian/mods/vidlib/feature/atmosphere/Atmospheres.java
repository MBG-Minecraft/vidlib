package dev.latvian.mods.vidlib.feature.atmosphere;

import dev.latvian.mods.klib.registry.Ref;

public interface Atmospheres {
	static Ref<Atmosphere> ref(String id) {
		return Atmosphere.REGISTRY.ref(id);
	}

	Ref<Atmosphere> DAY = ref("day");
	Ref<Atmosphere> DAY_WITH_CELESTIALS = ref("day_with_celestials");
	Ref<Atmosphere> NIGHT = ref("night");
	Ref<Atmosphere> NIGHT_WITH_CELESTIALS = ref("night_with_celestials");
	Ref<Atmosphere> STORM = ref("storm");
	Ref<Atmosphere> DOOM = ref("doom");
	Ref<Atmosphere> BRIGHT_NIGHT = ref("bright_night");

	Ref<Atmosphere> BLACK_VOID = ref("void/black");
	Ref<Atmosphere> WHITE_VOID = ref("void/white");
	Ref<Atmosphere> GREEN_VOID = ref("void/green");
	Ref<Atmosphere> BLUE_VOID = ref("void/blue");
}
