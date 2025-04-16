package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.core.ShimmerPayloadRegistrar;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.zone.Anchor;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		AutoInit.Type.GAME_LOADED.invoke();
	}

	@SubscribeEvent
	public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		var reg = ShimmerPayloadRegistrar.of(event);

		for (var s : AutoPacket.SCANNED.get()) {
			if (s.to().contains(AutoPacket.To.CLIENT) && s.to().contains(AutoPacket.To.SERVER)) {
				reg.bidi(s.type());
			} else if (s.to().contains(AutoPacket.To.CLIENT)) {
				reg.s2c(s.type());
			} else if (s.to().contains(AutoPacket.To.SERVER)) {
				reg.c2s(s.type());
			}
		}
	}

	@SubscribeEvent
	public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
			for (var entry : ShimmerTool.REGISTRY.entrySet()) {
				event.accept(entry.getValue().createFullItem());
			}

			for (var item : BuiltInRegistries.ITEM) {
				var mod = item.builtInRegistryHolder().getKey().location().getNamespace();

				if (mod.equals("video") || mod.equals(VidLib.ID) || mod.equals(Shimmer.ID)) {
					event.accept(item.getDefaultInstance());
				}
			}
		}
	}

	@SubscribeEvent
	public static void registerTicketControllers(RegisterTicketControllersEvent event) {
		event.register(Anchor.TICKET_CONTROLLER);
	}
}
