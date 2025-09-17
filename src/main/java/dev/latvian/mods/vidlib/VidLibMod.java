package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticles;
import dev.latvian.mods.vidlib.feature.platform.NeoPlatformHelper;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.file.Files;

@Mod(VidLib.ID)
public class VidLibMod {
	public VidLibMod(IEventBus bus) throws Exception {
		PlatformHelper.CURRENT = new NeoPlatformHelper();
		VidLib.init();

		if (Files.notExists(VidLib.DIR)) {
			Files.createDirectories(VidLib.DIR);
		}

		if (Files.notExists(VidLib.LOCAL_DIR)) {
			Files.createDirectories(VidLib.LOCAL_DIR);
		}

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

		if (ModList.get().isLoaded("video")) {
			VidLibConfig.strongEntityOutline = true;
		}
	}
}
