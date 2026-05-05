package dev.latvian.mods.vidlib.integration.iris;

import dev.latvian.mods.vidlib.VidLib;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public class IrisIntegration {
	public static IrisIntegration INSTANCE = new IrisIntegration();

	@SubscribeEvent
	public static void clientLoaded(FMLClientSetupEvent event) {
		if (ModList.get().isLoaded("iris")) {
			IrisIntegration.INSTANCE = new IrisIntegrationImpl();
		}
	}

	public boolean isShaderPackInUse() {
		return false;
	}
}
