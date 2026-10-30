package dev.latvian.mods.vidlib.feature.skybox;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.resources.Identifier;

public interface Skyboxes {
	Identifier VANILLA = ID.mc("vanilla");

	Identifier DAY = ID.mc("day");
	Identifier DAY_WITH_CELESTIALS = ID.mc("day_with_celestials");
	Identifier NIGHT = ID.mc("night");
	Identifier NIGHT_WITH_CELESTIALS = ID.mc("night_with_celestials");
	Identifier STORM = ID.mc("storm");
	Identifier DOOM = ID.mc("doom");
	Identifier BRIGHT_NIGHT = ID.mc("bright_night");

	Identifier BLACK_VOID = ID.mc("void/black");
	Identifier WHITE_VOID = ID.mc("void/white");
	Identifier GREEN_VOID = ID.mc("void/green");
	Identifier BLUE_VOID = ID.mc("void/blue");
}
