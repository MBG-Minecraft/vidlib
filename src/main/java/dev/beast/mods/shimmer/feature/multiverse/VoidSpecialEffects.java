package dev.beast.mods.shimmer.feature.multiverse;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class VoidSpecialEffects extends DimensionSpecialEffects {
	public VoidSpecialEffects() {
		super(Float.NaN, false, SkyType.NORMAL, false, false);
	}

	@Override
	@NotNull
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
	}

	@Override
	public boolean isFoggyAt(int x, int y) {
		return false;
	}
}
