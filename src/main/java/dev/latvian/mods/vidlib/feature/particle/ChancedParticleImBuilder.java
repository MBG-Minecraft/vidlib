package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.ParticleOptionsImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import net.minecraft.core.particles.ParticleOptions;

public class ChancedParticleImBuilder implements ImBuilder<ChancedParticle> {
	public static final ImBuilderType<ChancedParticle> TYPE = ChancedParticleImBuilder::new;

	private final ImBuilderWrapper<ParticleOptions> particle = ParticleOptionsImBuilder.create();
	private final ImBuilderWrapper<KNumber> chance = KNumberImBuilder.create(1F);

	@Override
	public void set(ChancedParticle value) {
		particle.set(value.particle());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		update = update.or(particle.imguiKey(graphics, "Particle", "particle"));
		update = update.or(chance.imguiKey(graphics, "Chance", "chance"));
		return update;
	}

	@Override
	public boolean isValid() {
		return particle.isValid() && chance.isValid();
	}

	@Override
	public ChancedParticle build() {
		return new ChancedParticle(particle.build(), chance.build());
	}
}
