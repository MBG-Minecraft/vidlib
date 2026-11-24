package dev.latvian.mods.vidlib.client;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.platform.neoforge.VLClientNeoPlatformHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(value = VidLib.ID, dist = Dist.CLIENT)
public class VidLibClientMod {
	public VidLibClientMod(ModContainer mod, IEventBus bus) {
		VLPlatformHelper.INSTANCE = new VLClientNeoPlatformHelper(mod);
	}

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event) {
		VidLibClient.init();
	}
}
