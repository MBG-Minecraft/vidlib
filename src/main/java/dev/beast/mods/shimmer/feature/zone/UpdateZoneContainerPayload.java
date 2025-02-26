package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.ShimmerNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record UpdateZoneContainerPayload(Optional<ZoneContainer> container) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateZoneContainerPayload> TYPE = ShimmerNet.type("update_zone_container");

	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateZoneContainerPayload> STREAM_CODEC = ByteBufCodecs.optional(ZoneContainer.STREAM_CODEC).map(UpdateZoneContainerPayload::new, UpdateZoneContainerPayload::container);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ZoneContainer.CLIENT = container.orElse(null));
	}
}
