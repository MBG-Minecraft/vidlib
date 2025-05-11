package dev.latvian.mods.vidlib.feature.client;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class ParticleRandom {
	public static final RandomSource SHARED = new XoroshiroRandomSource(1234L);
	public static RandomSource CURRENT = SHARED;
}
