package dev.latvian.mods.vidlib.feature.imgui.builder.particle;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;

public class ColorParticleOptionImBuilder implements ParticleOptionsImBuilder<ColorParticleOption> {
	public final ParticleType<ColorParticleOption> type;
	public final Color4ImBuilder color;

	public ColorParticleOptionImBuilder(ParticleType<ColorParticleOption> type) {
		this.type = type;
		this.color = new Color4ImBuilder();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return color.imgui(graphics);
	}

	@Override
	public ColorParticleOption build() {
		return ColorParticleOption.create(type, color.build().argb());
	}

	@Override
	public boolean isValid() {
		return color.isValid();
	}

	@Override
	public boolean isSmall() {
		return true;
	}
}
