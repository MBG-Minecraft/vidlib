package dev.latvian.mods.vidlib.feature.gradient;

import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.color.GradientReference;
import dev.latvian.mods.vidlib.feature.registry.ID;

public interface ClientGradients {
	Gradient TRAIL = new GradientReference(ID.mc("trail"));
	Gradient FIRE_1 = new GradientReference(ID.mc("fire/1"));
	Gradient FIRE_2 = new GradientReference(ID.mc("fire/2"));
	Gradient FIRE_3 = new GradientReference(ID.mc("fire/3"));
	Gradient FIRE_4 = new GradientReference(ID.mc("fire/4"));
	Gradient SPARK = new GradientReference(ID.mc("spark"));
}
