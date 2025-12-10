package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticles;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.platform.neoforge.NeoPlatformHelper;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(VidLib.ID)
public class VidLibMod {
	public VidLibMod(ModContainer mod, IEventBus bus) {
		PlatformHelper.CURRENT = new NeoPlatformHelper(mod);
		VidLib.VERSION = mod.getModInfo().getVersion().toString();
		VidLib.init();

		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof DeferredRegister<?> reg) {
				var container = ModList.get().getModContainerById(s.source());

				if (container.isPresent()) {
					var bus1 = container.get().getEventBus();

					if (bus1 != null) {
						reg.register(bus1);
					} else {
						VidLib.LOGGER.error("Failed to find @AutoRegister mod event bus for " + s.source());
					}
				} else {
					VidLib.LOGGER.error("Failed to find @AutoRegister mod container for " + s.source());
				}
			}
		}

		VidLibContent.init(bus);

		var particleRegistry = DeferredRegister.create(Registries.PARTICLE_TYPE, VidLib.ID);

		for (var particle : VidLibParticles.PARTICLES) {
			particleRegistry.register(particle.left(), particle.right());
		}

		particleRegistry.register(bus);
	}
}
