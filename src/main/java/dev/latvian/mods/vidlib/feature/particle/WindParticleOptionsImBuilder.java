package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.interpolation.Interpolation;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.interpolation.InterpolationImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.ParticleOptionsImBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;

public class WindParticleOptionsImBuilder implements ParticleOptionsImBuilder<WindParticleOptions> {
	public final ImInt lifespan = new ImInt(100);
	public final ImBoolean ground = new ImBoolean(false);
	public final ImFloat scale = new ImFloat(1F);
	public final ImBuilder<Interpolation> interpolation = InterpolationImBuilder.create();

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		ImGui.text("Lifespan");
		ImGui.inputInt("###lifespan", lifespan);
		update = update.orItemEdit();
		update = update.or(ImGui.checkbox("Ground", ground));
		ImGui.text("Scale");
		ImGui.sliderFloat("###scale", scale.getData(), 0F, 10F);
		update = update.orItemEdit();
		update = update.or(interpolation.imguiKey(graphics, "Interpolation", "interpolation"));
		return update;
	}

	@Override
	public boolean isValid() {
		return interpolation.isValid();
	}

	@Override
	public WindParticleOptions build() {
		return new WindParticleOptions(lifespan.get(), ground.get(), scale.get(), interpolation.build());
	}
}
