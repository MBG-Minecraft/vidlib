package dev.latvian.mods.vidlib.feature.atmosphere;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.ResourceKey;

public interface Atmospheres {
	static ResourceKey<Atmosphere> create(String id) {
		return Atmosphere.createId(ID.vidlib(id));
	}

	ResourceKey<Atmosphere> DAY = create("day");
	ResourceKey<Atmosphere> DAY_WITH_CELESTIALS = create("day_with_celestials");
	ResourceKey<Atmosphere> NIGHT = create("night");
	ResourceKey<Atmosphere> NIGHT_WITH_CELESTIALS = create("night_with_celestials");
	ResourceKey<Atmosphere> STORM = create("storm");
	ResourceKey<Atmosphere> DOOM = create("doom");
	ResourceKey<Atmosphere> BRIGHT_NIGHT = create("bright_night");

	ResourceKey<Atmosphere> BLACK_VOID = create("void/black");
	ResourceKey<Atmosphere> WHITE_VOID = create("void/white");
	ResourceKey<Atmosphere> GREEN_VOID = create("void/green");
	ResourceKey<Atmosphere> BLUE_VOID = create("void/blue");
}
