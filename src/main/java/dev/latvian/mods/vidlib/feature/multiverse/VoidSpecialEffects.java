package dev.latvian.mods.vidlib.feature.multiverse;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class VoidSpecialEffects extends DimensionSpecialEffects {
	public VoidSpecialEffects() {
		super(Float.NaN, false, SkyType.OVERWORLD, false, false);
	}

	@Override
	public boolean isSunriseOrSunset(float timeOfDay) {
		float f = Mth.cos(timeOfDay * ((float) Math.PI * 2F));
		return f >= -0.4F && f <= 0.4F;
	}

	@Override
	public int getSunriseOrSunsetColor(float timeOfDay) {
		float f = Mth.cos((float) (timeOfDay * Math.PI * 2D));
		float f1 = f / 0.4F * 0.5F + 0.5F;
		float f2 = Mth.square(1.0F - (1.0F - Mth.sin(f1 * (float) Math.PI)) * 0.99F);
		return ARGB.colorFromFloat(f2, f1 * 0.3F + 0.7F, f1 * f1 * 0.7F + 0.2F, 0.2F);
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
