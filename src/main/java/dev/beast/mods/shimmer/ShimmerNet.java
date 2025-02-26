package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.content.clock.ClockPayload;
import dev.beast.mods.shimmer.feature.zone.UpdateZoneContainerPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public interface ShimmerNet {
	static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String id) {
		return new CustomPacketPayload.Type<>(Shimmer.id(id));
	}

	@SubscribeEvent
	static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		var reg = event.registrar("1").optional();

		reg.playToClient(ClockPayload.TYPE, ClockPayload.STREAM_CODEC, ClockPayload::handle);
		reg.playToClient(UpdateZoneContainerPayload.TYPE, UpdateZoneContainerPayload.STREAM_CODEC, UpdateZoneContainerPayload::handle);
	}
}
