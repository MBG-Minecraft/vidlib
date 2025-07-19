package dev.latvian.mods.vidlib.feature.imgui.builder.particle;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color3ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import net.minecraft.core.particles.DustParticleOptions;

public class DustParticleOptionImBuilder implements ParticleOptionsImBuilder<DustParticleOptions> {
	public final Color3ImBuilder color;
	public final FloatImBuilder scale;

	public DustParticleOptionImBuilder() {
		this.color = new Color3ImBuilder();
		this.scale = new FloatImBuilder(1F, 0F, 2F);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		update = update.or(color.imguiKey(graphics, "Color", "color"));
		update = update.or(scale.imguiKey(graphics, "Scale", "scale"));
		return update;
	}

	@Override
	public DustParticleOptions build() {
		return new DustParticleOptions(color.build().rgb(), scale.build());
	}

	@Override
	public boolean isValid() {
		return color.isValid() && scale.isValid();
	}
}
