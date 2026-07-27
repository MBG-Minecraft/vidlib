package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.imgui.builder.particle.BlockParticleOptionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.ColorParticleOptionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.DustParticleOptionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.ParticleOptionsImBuilderRegistryEvent;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticles;
import dev.latvian.mods.vidlib.feature.particle.WindParticleOptionsImBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber(modid = VidLib.ID)
public class ImBuilderRegistryEventHandler {
	@SubscribeEvent
	public static void particleImBuilders(ParticleOptionsImBuilderRegistryEvent event) {
		event.register(List.of(
			ParticleTypes.BLOCK,
			ParticleTypes.BLOCK_MARKER,
			ParticleTypes.FALLING_DUST,
			ParticleTypes.DUST_PILLAR,
			ParticleTypes.BLOCK_CRUMBLE
		), BlockParticleOptionImBuilder::new);

		event.register(ParticleTypes.DUST, t -> new DustParticleOptionImBuilder());

		event.register(List.of(
			ParticleTypes.ENTITY_EFFECT,
			ParticleTypes.TINTED_LEAVES
		), ColorParticleOptionImBuilder::new);

		event.register(VidLibParticles.WIND.get(), t -> new WindParticleOptionsImBuilder());
	}
}
